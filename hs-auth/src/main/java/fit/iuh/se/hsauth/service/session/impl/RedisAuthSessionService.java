package fit.iuh.se.hsauth.service.session.impl;

import fit.iuh.se.hsauth.dto.session.AuthSession;
import fit.iuh.se.hsauth.service.session.AuthSessionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedisAuthSessionService implements AuthSessionService {

    static String KEY_PREFIX = "auth:session:";
    static String USER_SESSIONS_KEY_PREFIX = "auth:user-sessions:";
    static String USER_ID = "userId";
    static String SESSION_ID = "sessionId";
    static String REFRESH_TOKEN_HASH = "refreshTokenHash";
    static String CREATED_AT = "createdAt";
    static String EXPIRES_AT = "expiresAt";

    StringRedisTemplate redisTemplate;

    @Override
    public void save(AuthSession session, Duration ttl) {
        String sessionKey = key(session.getUserId(), session.getSessionId());
        String userSessionsKey = userSessionsKey(session.getUserId());
        redisTemplate.opsForHash().putAll(sessionKey, Map.of(
                USER_ID, session.getUserId().toString(),
                SESSION_ID, session.getSessionId(),
                REFRESH_TOKEN_HASH, session.getRefreshTokenHash(),
                CREATED_AT, session.getCreatedAt().toString(),
                EXPIRES_AT, session.getExpiresAt().toString()
        ));
        redisTemplate.expire(sessionKey, ttl);
        redisTemplate.opsForSet().add(userSessionsKey, session.getSessionId());
        redisTemplate.expire(userSessionsKey, ttl);
    }

    @Override
    public Optional<AuthSession> findById(Long userId, String sessionId) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key(userId, sessionId));
        if (entries.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new AuthSession(
                Long.valueOf(entries.get(USER_ID).toString()),
                entries.get(SESSION_ID).toString(),
                entries.get(REFRESH_TOKEN_HASH).toString(),
                Instant.parse(entries.get(CREATED_AT).toString()),
                Instant.parse(entries.get(EXPIRES_AT).toString())
        ));
    }

    @Override
    public void updateRefreshTokenHash(Long userId, String sessionId, String refreshTokenHash, Duration ttl) {
        redisTemplate.opsForHash().put(key(userId, sessionId), REFRESH_TOKEN_HASH, refreshTokenHash);
        redisTemplate.expire(key(userId, sessionId), ttl);
        redisTemplate.opsForSet().add(userSessionsKey(userId), sessionId);
        redisTemplate.expire(userSessionsKey(userId), ttl);
    }

    @Override
    public void revoke(Long userId, String sessionId) {
        redisTemplate.delete(key(userId, sessionId));
        redisTemplate.opsForSet().remove(userSessionsKey(userId), sessionId);
    }

    @Override
    public void revokeAll(Long userId) {
        String userSessionsKey = userSessionsKey(userId);
        Set<String> sessionIds = redisTemplate.opsForSet().members(userSessionsKey);
        Set<String> sessionKeys = new HashSet<>();
        if (sessionIds != null) {
            sessionIds.stream()
                    .map(sessionId -> key(userId, sessionId))
                    .forEach(sessionKeys::add);
        }

        // Also scan for sessions created before the per-user index was introduced.
        try (var keys = redisTemplate.scan(ScanOptions.scanOptions()
                .match(KEY_PREFIX + userId + ":*")
                .count(100)
                .build()).stream()) {
            keys.forEach(sessionKeys::add);
        }
        if (!sessionKeys.isEmpty()) redisTemplate.delete(sessionKeys);
        redisTemplate.delete(userSessionsKey);
    }

    private String key(Long userId, String sessionId) {
        return KEY_PREFIX + userId + ":" + sessionId;
    }

    private String userSessionsKey(Long userId) {
        return USER_SESSIONS_KEY_PREFIX + userId;
    }
}
