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
      "/auth/refresh", "refresh",
      "/auth/refresh-with-cookie", "refresh");

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {
    String policyName = HttpMethod.POST.matches(request.getMethod())
        ? POST_POLICIES.get(request.getRequestURI()) : null;

    // Unconfigured routes and CORS preflight requests pass without consuming a quota.
    if (policyName == null) {
      filterChain.doFilter(request, response);
      return;
    }

    RateLimitProperties.Limit limit = properties.forPolicy(policyName);
    // The bundled Nginx proxy overwrites X-Real-IP with the connecting client address.
    // The backend must only trust this header when requests come through that proxy.
    String clientIp = request.getHeader("X-Real-IP");
    if (clientIp == null || clientIp.isBlank()) {
      clientIp = request.getRemoteAddr();
    }
    String key = policyName + ":" + clientIp.trim();
    Duration retryAfter;
    try {
      retryAfter = rateLimitStore.tryAcquire(key, limit.getMaxRequests(), limit.getWindow());
    } catch (RedisConnectionFailureException exception) {
      // Fail closed for protected auth endpoints, but report infrastructure failure accurately.
      response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
      response.setHeader(HttpHeaders.RETRY_AFTER, "5");
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.getWriter().write("{\"status\":503,\"message\":\"Rate limit service is temporarily unavailable.\"}");
      return;
    }
    if (retryAfter != null) {
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.setHeader(HttpHeaders.RETRY_AFTER, Long.toString(Math.max(1, retryAfter.toSeconds())));
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.getWriter().write("{\"status\":429,\"message\":\"Too many requests. Please retry later.\"}");
      return;
    }

    filterChain.doFilter(request, response);
  }
}
