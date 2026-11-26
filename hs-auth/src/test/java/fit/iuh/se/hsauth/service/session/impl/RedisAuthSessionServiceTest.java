package fit.iuh.se.hsauth.service.session.impl;

import fit.iuh.se.hsauth.dto.session.AuthSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisAuthSessionServiceTest {

    @Mock StringRedisTemplate redisTemplate;
    @Mock HashOperations<String, Object, Object> hashOps;
    @Mock SetOperations<String, String> setOps;

    RedisAuthSessionService service;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForHash()).thenReturn(hashOps);
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOps);
        service = new RedisAuthSessionService(redisTemplate);
    }

    @Test
    void savePrunesExpiredSessionIdsFromUserSessionsSet() {
        Long userId = 100L;
        AuthSession session = new AuthSession(userId, "sid-new", "hash", Instant.now(), Instant.now().plusSeconds(3600));
        when(setOps.members("auth:user-sessions:100")).thenReturn(Set.of("sid-new", "sid-live", "sid-dead"));
        when(redisTemplate.hasKey("auth:session:100:sid-live")).thenReturn(true);
        when(redisTemplate.hasKey("auth:session:100:sid-dead")).thenReturn(false);

        service.save(session, Duration.ofHours(1));

        verify(setOps).add("auth:user-sessions:100", "sid-new");
        verify(setOps).remove("auth:user-sessions:100", new Object[]{"sid-dead"});
        verify(redisTemplate).expire("auth:user-sessions:100", Duration.ofHours(1));
    }

    @Test
    void findByIdRemovesStaleSessionIdWhenHashIsMissing() {
        when(hashOps.entries("auth:session:100:sid-expired")).thenReturn(Map.of());

        assertTrue(service.findById(100L, "sid-expired").isEmpty());
        verify(setOps).remove("auth:user-sessions:100", "sid-expired");
    }
}
