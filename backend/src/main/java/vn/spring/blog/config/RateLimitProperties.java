package vn.spring.blog.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {
  private Limit login = new Limit(10, Duration.ofMinutes(1));
  private Limit register = new Limit(5, Duration.ofHours(1));
  private Limit refresh = new Limit(30, Duration.ofMinutes(1));

  public Limit forPolicy(String name) {
    return switch (name) {
      case "login" -> login;
      case "register" -> register;
      case "refresh" -> refresh;
      default -> throw new IllegalArgumentException("Unknown rate limit policy: " + name);
    };
  }

  public Limit getLogin() { return login; }
  public void setLogin(Limit login) { this.login = login; }
  public Limit getRegister() { return register; }
  public void setRegister(Limit register) { this.register = register; }
  public Limit getRefresh() { return refresh; }
  public void setRefresh(Limit refresh) { this.refresh = refresh; }

  public static class Limit {
    private int maxRequests;
    private Duration window;

    public Limit() {}
    public Limit(int maxRequests, Duration window) {
      this.maxRequests = maxRequests;
      this.window = window;
    }
    public int getMaxRequests() { return maxRequests; }
    public void setMaxRequests(int maxRequests) { this.maxRequests = maxRequests; }
    public Duration getWindow() { return window; }
    public void setWindow(Duration window) { this.window = window; }
  }
}
