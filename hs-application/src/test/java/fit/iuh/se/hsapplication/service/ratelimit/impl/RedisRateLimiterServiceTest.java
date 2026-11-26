package fit.iuh.se.hsapplication.service.ratelimit.impl;

import fit.iuh.se.hsapplication.dto.ratelimit.RateLimitResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisRateLimiterServiceTest {

    private StringRedisTemplate redisTemplate;
    private RedisRateLimiterService service;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        service = new RedisRateLimiterService(redisTemplate);
        ReflectionTestUtils.setField(service, "capacity", 100L);
        ReflectionTestUtils.setField(service, "windowSeconds", 60L);
    }

    @Test
    @SuppressWarnings("unchecked")
    void allowsRequestWithinAtomicWindow() {
        when(redisTemplate.execute(any(RedisScript.class), eq(List.of("rate-limit:user:1")), eq("60")))
                .thenReturn(List.of(3L, 55L));

        RateLimitResult result = service.consume("user:1");

        assertThat(result.isAllowed()).isTrue();
        assertThat(result.getLimit()).isEqualTo(100L);
        assertThat(result.getRemaining()).isEqualTo(97L);
        assertThat(result.getRetryAfterSeconds()).isEqualTo(55L);
    }

    @Test
    @SuppressWarnings("unchecked")
    void blocksRequestWhenOverCustomCapacity() {
        when(redisTemplate.execute(any(RedisScript.class), eq(List.of("rate-limit:assistant:user:7")), eq("60")))
                .thenReturn(List.of(21L, 42L));

        RateLimitResult result = service.consume("assistant:user:7", 20L, 60L);

        assertThat(result.isAllowed()).isFalse();
        assertThat(result.getLimit()).isEqualTo(20L);
        assertThat(result.getRemaining()).isZero();
        assertThat(result.getRetryAfterSeconds()).isEqualTo(42L);
        verify(redisTemplate).execute(any(RedisScript.class), eq(List.of("rate-limit:assistant:user:7")), eq("60"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void failsOpenWhenRedisThrowsException() {
        when(redisTemplate.execute(any(RedisScript.class), any(), any()))
                .thenThrow(new RuntimeException("Redis down"));

        RateLimitResult result = service.consume("ip:127.0.0.1");

        assertThat(result.isAllowed()).isTrue();
        assertThat(result.getRemaining()).isEqualTo(100L);
    }
}
