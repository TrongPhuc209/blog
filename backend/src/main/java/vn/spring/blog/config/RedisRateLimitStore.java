package vn.spring.blog.config;

import java.time.Duration;
import java.util.List;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** Fixed-window counters shared by all application instances through Redis. */
@Component
@RequiredArgsConstructor
public class RedisRateLimitStore {
  @SuppressWarnings("rawtypes")
  private static final DefaultRedisScript<List> INCREMENT_AND_SET_TTL = new DefaultRedisScript<>(
      "local count = redis.call('INCR', KEYS[1]) " +
      "if count == 1 then redis.call('PEXPIRE', KEYS[1], ARGV[1]) end " +
      "local ttl = redis.call('PTTL', KEYS[1]) " +
      "return {count, ttl}",
      List.class);

  private final StringRedisTemplate redisTemplate;

  /** Returns null when allowed; otherwise returns the time remaining in the active window. */
  public Duration tryAcquire(String key, int limit, Duration window) {
    long windowMillis = Math.max(1, window.toMillis());
    List<?> result = redisTemplate.execute(
        INCREMENT_AND_SET_TTL,
        List.of("rate-limit:" + key),
        Long.toString(windowMillis));

    if (result == null || result.size() < 2) {
      throw new IllegalStateException("Redis did not return the rate limit counter state");
    }

    long count = ((Number) result.get(0)).longValue();
    long remainingMillis = ((Number) result.get(1)).longValue();
    if (count <= limit) {
      return null;
    }
    return Duration.ofMillis(Math.max(1, remainingMillis));
  }
}
