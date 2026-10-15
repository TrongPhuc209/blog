package vn.spring.blog.config;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/** Applies small, IP-based limits to public authentication endpoints. */
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {
  private final RedisRateLimitStore rateLimitStore;
  private final RateLimitProperties properties;

  private static final Map<String, String> POST_POLICIES = Map.of(
    "/auth/login", "login",
    "/auth/register", "register",
    "/auth/refresh-with-cookie", "refresh",
    "/auth/refresh", "refresh"
  );

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    @SuppressWarnings("null")
    String policyName = HttpMethod.POST.matches(request.getMethod()) ? POST_POLICIES.get(request.getRequestURI()) : null;
    if(policyName == null || policyName.isBlank()){
      filterChain.doFilter(request, response);
      return;
    }
    
    RateLimitProperties.Limit limit = properties.forPolicy(policyName);
    String clientIp = request.getHeader("X-Real-IP");
    if(clientIp == null || clientIp.isBlank()){
      clientIp = request.getRemoteAddr();
    }

    String key = request.getRequestURI() + ":" + clientIp.trim();
    Duration retryAfter;
    try{
      retryAfter = rateLimitStore.tryAcquire(key, limit.getMaxRequests(), limit.getWindow());
    } catch(RedisConnectionFailureException exception) {
        response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
        response.setHeader(HttpHeaders.RETRY_AFTER, "5");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"status\":503,\"message\":\"Rate limit service is temporarily unavailable.\"}");
        return;
    }
    
    if(retryAfter != null){
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader(HttpHeaders.RETRY_AFTER, Long.toString(Math.max(1, retryAfter.toSeconds())));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"status\":429,\"message\":\"Too many requests. Please retry later.\"}");
        return; 
    }
    filterChain.doFilter(request, response);
  }
}
