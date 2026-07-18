package fit.iuh.se.hsapplication.service.ratelimit.impl;

import fit.iuh.se.hsapplication.dto.ratelimit.RateLimitResult;
import fit.iuh.se.hsapplication.service.ratelimit.RateLimiterService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RedisRateLimiterService implements RateLimiterService {

    private static final String KEY_PREFIX = "rate-limit:";

    final StringRedisTemplate redisTemplate;

    @Value("${app.rate-limit.capacity}")
    long capacity;

    @Value("${app.rate-limit.window-seconds}")
    long windowSeconds;

    @Override
    public RateLimitResult consume(String key) {
        String redisKey = KEY_PREFIX + key;

        try {
            Long current = redisTemplate.opsForValue().increment(redisKey);
            if (current != null && current == 1L) {
                redisTemplate.expire(redisKey, Duration.ofSeconds(windowSeconds));
            }

            long used = current == null ? 0L : current;
            long remaining = Math.max(capacity - used, 0L);
            long retryAfter = retryAfter(redisKey);
            return new RateLimitResult(used <= capacity, capacity, remaining, retryAfter);
        } catch (RuntimeException exception) {
            log.warn("Rate limiter failed for key {}. Request is allowed.", key, exception);
            return new RateLimitResult(true, capacity, capacity, 0L);
        }
    }

    private long retryAfter(String redisKey) {
        Long ttl = redisTemplate.getExpire(redisKey);
        if (ttl == null || ttl < 0) {
            return windowSeconds;
        }
        return ttl;
    }
}
