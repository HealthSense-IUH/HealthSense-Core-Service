package fit.iuh.se.hsapplication.service.ratelimit.impl;

import fit.iuh.se.hsapplication.dto.ratelimit.RateLimitResult;
import fit.iuh.se.hsapplication.service.ratelimit.RateLimiterService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RedisRateLimiterService implements RateLimiterService {

    private static final String KEY_PREFIX = "rate-limit:";

    /**
     * Atomic INCR + EXPIRE + TTL trong 1 round-trip Redis.
     * Nếu key mới tạo (current == 1) hoặc mất TTL (ttl < 0 do lỗi trước đó), tự đặt lại EXPIRE để tránh khóa vĩnh viễn.
     */
    @SuppressWarnings("rawtypes")
    private static final DefaultRedisScript<List> ATOMIC_CONSUME_SCRIPT = new DefaultRedisScript<>(
            "local current = redis.call('INCR', KEYS[1]) " +
                    "local ttl = redis.call('TTL', KEYS[1]) " +
                    "if current == 1 or ttl < 0 then " +
                    "  redis.call('EXPIRE', KEYS[1], tonumber(ARGV[1])) " +
                    "  ttl = tonumber(ARGV[1]) " +
                    "end " +
                    "return {current, ttl}",
            List.class
    );

    final StringRedisTemplate redisTemplate;

    @Value("${app.rate-limit.capacity}")
    long capacity;

    @Value("${app.rate-limit.window-seconds}")
    long windowSeconds;

    @Override
    public RateLimitResult consume(String key) {
        return consume(key, capacity, windowSeconds);
    }

    @Override
    public RateLimitResult consume(String key, long customCapacity, long customWindowSeconds) {
        long effectiveCapacity = Math.max(1L, customCapacity);
        long effectiveWindow = Math.max(1L, customWindowSeconds);
        String redisKey = KEY_PREFIX + key;

        try {
            List<?> result = redisTemplate.execute(
                    ATOMIC_CONSUME_SCRIPT,
                    List.of(redisKey),
                    String.valueOf(effectiveWindow)
            );
            long used = extractLong(result, 0, 1L);
            long ttl = extractLong(result, 1, effectiveWindow);
            long remaining = Math.max(effectiveCapacity - used, 0L);
            long retryAfter = ttl < 0 ? effectiveWindow : ttl;
            return new RateLimitResult(used <= effectiveCapacity, effectiveCapacity, remaining, retryAfter);
        } catch (RuntimeException exception) {
            log.warn("Rate limiter failed for key {}. Request is allowed.", key, exception);
            return new RateLimitResult(true, effectiveCapacity, effectiveCapacity, 0L);
        }
    }

    private static long extractLong(List<?> list, int index, long fallback) {
        if (list == null || index >= list.size() || list.get(index) == null) {
            return fallback;
        }
        Object value = list.get(index);
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
