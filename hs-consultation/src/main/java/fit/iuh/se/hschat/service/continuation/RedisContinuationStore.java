package fit.iuh.se.hschat.service.continuation;

import fit.iuh.se.hschat.entity.enums.ContinuationDecision;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RedisContinuationStore implements ContinuationStore {

    static final String PREFIX = "consultation:dispatch:continuation:";

    private static final DefaultRedisScript<Long> OPEN = new DefaultRedisScript<>("""
            if redis.call('EXISTS', KEYS[1]) == 1 then
              if redis.call('HGET', KEYS[1], 'sessionId') == ARGV[1]
                and redis.call('HGET', KEYS[1], 'round') == ARGV[2] then return 2 end
              return -1
            end
            redis.call('HSET', KEYS[1],
              'sessionId', ARGV[1], 'round', ARGV[2],
              'doctorDecision', 'PENDING', 'memberDecision', 'PENDING',
              'promptedAt', ARGV[3], 'graceExpiresAt', ARGV[4])
            redis.call('PEXPIREAT', KEYS[1], ARGV[5])
            return 1
            """, Long.class);

    private static final DefaultRedisScript<Long> DECIDE = new DefaultRedisScript<>("""
            if redis.call('HGET', KEYS[1], 'sessionId') ~= ARGV[1]
              or redis.call('HGET', KEYS[1], 'round') ~= ARGV[2] then return -1 end
            local deadline = tonumber(redis.call('HGET', KEYS[1], 'graceExpiresAt'))
            if deadline == nil or tonumber(ARGV[5]) >= deadline then return -2 end
            local field = ARGV[3]
            local current = redis.call('HGET', KEYS[1], field)
            if current == ARGV[4] then return 2 end
            if current ~= 'PENDING' then return -3 end
            redis.call('HSET', KEYS[1], field, ARGV[4])
            return 1
            """, Long.class);

    private static final DefaultRedisScript<Long> RELEASE = new DefaultRedisScript<>("""
            if redis.call('HGET', KEYS[1], 'sessionId') == ARGV[1]
              and redis.call('HGET', KEYS[1], 'round') == ARGV[2] then
              return redis.call('DEL', KEYS[1])
            end
            return 0
            """, Long.class);

    private final StringRedisTemplate redis;

    @Value("${app.consultation.continuation.recovery-tail-minutes:30}")
    long recoveryTailMinutes;

    @Override
    public OpenResult open(Long sessionId, int round, Instant promptedAt, Instant graceExpiresAt) {
        Instant expiresAt = graceExpiresAt.plus(Duration.ofMinutes(recoveryTailMinutes));
        Long result = execute(OPEN, List.of(key(sessionId, round)), sessionId.toString(),
                Integer.toString(round), millis(promptedAt), millis(graceExpiresAt), millis(expiresAt));
        return result == 1 ? OpenResult.CREATED : result == 2 ? OpenResult.ALREADY_OPEN : OpenResult.STALE;
    }

    @Override
    public Optional<ContinuationState> find(Long sessionId, int round) {
        Map<Object, Object> values = safe(() -> redis.opsForHash().entries(key(sessionId, round)));
        if (values == null || values.isEmpty()) return Optional.empty();
        try {
            if (!sessionId.equals(Long.valueOf(value(values, "sessionId")))
                    || round != Integer.parseInt(value(values, "round"))) return Optional.empty();
            return Optional.of(toState(values));
        } catch (RuntimeException exception) {
            throw unavailable(exception);
        }
    }

    @Override
    public DecisionOutcome decide(Long sessionId, int round, UserRole actorRole,
            ContinuationDecision decision, Instant now) {
        String field = actorRole == UserRole.DOCTOR ? "doctorDecision" : "memberDecision";
        Long result = execute(DECIDE, List.of(key(sessionId, round)), sessionId.toString(),
                Integer.toString(round), field, decision.name(), millis(now));
        DecisionResult mapped = result == 1 ? DecisionResult.ACCEPTED
                : result == 2 ? DecisionResult.IDEMPOTENT
                : result == -2 ? DecisionResult.EXPIRED
                : result == -3 ? DecisionResult.CONFLICT : DecisionResult.STALE;
        ContinuationState state = mapped == DecisionResult.STALE ? null : find(sessionId, round).orElse(null);
        return new DecisionOutcome(mapped, state);
    }

    @Override
    public boolean release(Long sessionId, int round) {
        return execute(RELEASE, List.of(key(sessionId, round)), sessionId.toString(),
                Integer.toString(round)) > 0;
    }

    private ContinuationState toState(Map<Object, Object> values) {
        return new ContinuationState(
                Long.valueOf(value(values, "sessionId")),
                Integer.parseInt(value(values, "round")),
                ContinuationDecision.valueOf(value(values, "doctorDecision")),
                ContinuationDecision.valueOf(value(values, "memberDecision")),
                Instant.ofEpochMilli(Long.parseLong(value(values, "promptedAt"))),
                Instant.ofEpochMilli(Long.parseLong(value(values, "graceExpiresAt"))));
    }

    private Long execute(DefaultRedisScript<Long> script, List<String> keys, String... arguments) {
        Long result = safe(() -> redis.execute(script, keys, (Object[]) arguments));
        if (result == null) throw unavailable(null);
        return result;
    }

    private <T> T safe(Action<T> action) {
        try { return action.get(); }
        catch (AppException exception) { throw exception; }
        catch (RuntimeException exception) { throw unavailable(exception); }
    }

    private AppException unavailable(Throwable cause) {
        return new AppException(ErrorCode.DISPATCH_TEMPORARILY_UNAVAILABLE,
                cause == null ? "Redis continuation state is unavailable"
                        : "Redis continuation state is unavailable: " + cause.getClass().getSimpleName());
    }

    private String key(Long sessionId, int round) { return PREFIX + sessionId + ":" + round; }
    private String millis(Instant value) { return Long.toString(value.toEpochMilli()); }
    private String value(Map<Object, Object> values, String name) { return Objects.toString(values.get(name)); }

    @FunctionalInterface
    private interface Action<T> { T get(); }
}
