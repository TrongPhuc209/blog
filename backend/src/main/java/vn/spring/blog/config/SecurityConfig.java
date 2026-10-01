package vn.spring.blog.config;

import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.util.Base64;

import vn.spring.blog.helper.exception.CustomAccessDeniedHandler;
import vn.spring.blog.helper.exception.CustomAuthenticationEntryPoint;
import vn.spring.blog.service.UserService;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
  @Value("${jwt.base64-secret}")
  String jwtKey;

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  UserDetailsService userDetailsService(UserService userService) {
    return new CustomUserDetailsService(userService);
  }

  @Bean
  AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider dao = new DaoAuthenticationProvider(userDetailsService);
    dao.setPasswordEncoder(passwordEncoder);
    return new ProviderManager(dao);
  }

  @Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000",
				"http://127.0.0.1:3000", "http://localhost:4173", "http://localhost:5173",
				"https://yourdomain.com"));

		configuration.setAllowedMethods(
				Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

		configuration.setAllowedHeaders(
				Arrays.asList("Authorization", "Content-Type", "Cache-Control"));

		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http,
      CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
      CustomAccessDeniedHandler customAccessDeniedHandler) throws Exception {

    http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
    
    String[] WHITELIST = {"/auth/login",
                         "/auth/refresh",
                          "/auth/refresh-with-cookie",
                          "/auth/register",
                          // swagger
                          "/v3/api-docs/**",
                          "/swagger-ui/**",
                          "/swagger-id.html",
    };

    String[] ADMIN_WHITELIST = {"/users/**",
                                "/roles/**",
    };

    String[] ADMIN_PUT_WHITELIST = {"/tags/{id}"
    };

    String[] ADMIN_DELETE_WHITELIST = {"/tags/{id}"
    };

    http.authorizeHttpRequests((request) -> request.requestMatchers(WHITELIST).permitAll()
        .requestMatchers(ADMIN_WHITELIST).hasRole("ADMIN")
        .requestMatchers(HttpMethod.PUT, ADMIN_PUT_WHITELIST).hasRole("ADMIN")
        .requestMatchers(HttpMethod.DELETE, ADMIN_DELETE_WHITELIST).hasRole("ADMIN")
        .anyRequest().authenticated());

    http.csrf(c -> c.disable());

    http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    http.oauth2ResourceServer(oauth2 -> oauth2
        .authenticationEntryPoint(customAuthenticationEntryPoint)
        .accessDeniedHandler(customAccessDeniedHandler)
        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

    return http.build();
  }

  @Bean
  JwtEncoder jwtEncoder() {
    return new NimbusJwtEncoder(new ImmutableSecret<>(getSecretKey()));
  }

  private SecretKey getSecretKey() {
    byte[] keyBytes = Base64.from(jwtKey).decode();

    return new SecretKeySpec(keyBytes, 0, keyBytes.length, JwtService.JWT_ALGORITHM.getName());
  }

  @Bean
  JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder.withSecretKey(getSecretKey()).macAlgorithm(JwtService.JWT_ALGORITHM).build();
  }

  JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();
    scopeConverter.setAuthoritiesClaimName("scope");
    scopeConverter.setAuthorityPrefix("");

    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(scopeConverter);
    return converter;
  }

}
