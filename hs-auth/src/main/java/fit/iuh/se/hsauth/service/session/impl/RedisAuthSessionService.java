package fit.iuh.se.hsauth.service.session.impl;

import fit.iuh.se.hsauth.dto.session.AuthSession;
import fit.iuh.se.hsauth.service.session.AuthSessionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedisAuthSessionService implements AuthSessionService {

    static String KEY_PREFIX = "auth:session:";
    static String USER_ID = "userId";
    static String SESSION_ID = "sessionId";
    static String REFRESH_TOKEN_HASH = "refreshTokenHash";
    static String CREATED_AT = "createdAt";
    static String EXPIRES_AT = "expiresAt";

    StringRedisTemplate redisTemplate;

    @Override
    public void save(AuthSession session, Duration ttl) {
        redisTemplate.opsForHash().putAll(key(session.getUserId(), session.getSessionId()), Map.of(
                USER_ID, session.getUserId().toString(),
                SESSION_ID, session.getSessionId(),
                REFRESH_TOKEN_HASH, session.getRefreshTokenHash(),
                CREATED_AT, session.getCreatedAt().toString(),
                EXPIRES_AT, session.getExpiresAt().toString()
        ));
        redisTemplate.expire(key(session.getUserId(), session.getSessionId()), ttl);
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
    }

    @Override
    public void revoke(Long userId, String sessionId) {
        redisTemplate.delete(key(userId, sessionId));
    }

    private String key(Long userId, String sessionId) {
        return KEY_PREFIX + userId + ":" + sessionId;
    }
}
