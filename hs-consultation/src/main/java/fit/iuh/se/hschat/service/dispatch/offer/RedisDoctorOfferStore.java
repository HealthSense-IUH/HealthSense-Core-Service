package fit.iuh.se.hschat.service.dispatch.offer;

import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class RedisDoctorOfferStore implements DoctorOfferStore {
    static final String OFFER_PREFIX = "consultation:dispatch:offer:";
    static final String DOCTOR_PREFIX = "consultation:dispatch:doctor:";
    static final String QUEUE_PREFIX = "consultation:dispatch:queue:";
    static final String DOCTOR_DEADLINES = "consultation:dispatch:doctor-offer-deadlines";
    static final String MEMBER_DEADLINES = "consultation:dispatch:member-confirmation-deadlines";

    private static final DefaultRedisScript<Long> CREATE = new DefaultRedisScript<>("""
            if redis.call('EXISTS', KEYS[1]) == 1 then return 3 end
            if redis.call('EXISTS', KEYS[2]) == 1 then return 2 end
            if redis.call('EXISTS', KEYS[3]) == 1 then return 3 end
            redis.call('HSET', KEYS[1],
              'offerId', ARGV[1], 'queueEntryId', ARGV[2], 'requestId', ARGV[3],
              'memberId', ARGV[4], 'doctorId', ARGV[5], 'state', ARGV[6],
              'offeredAt', ARGV[7], 'doctorOfferExpiresAt', ARGV[8])
            redis.call('PEXPIRE', KEYS[1], ARGV[9])
            redis.call('SET', KEYS[2], ARGV[1], 'PX', ARGV[9])
            redis.call('SET', KEYS[3], ARGV[1], 'PX', ARGV[9])
            redis.call('ZADD', KEYS[4], ARGV[10], ARGV[1])
            return 1
            """, Long.class);

    private static final DefaultRedisScript<Long> ACCEPT = new DefaultRedisScript<>("""
            if redis.call('GET', KEYS[2]) ~= ARGV[1] or redis.call('GET', KEYS[3]) ~= ARGV[1] then return -1 end
            local state = redis.call('HGET', KEYS[1], 'state')
            if state == 'WAITING_MEMBER_CONFIRMATION' then return 2 end
            if state ~= 'OFFERED_TO_DOCTOR' then return -1 end
            local deadline = tonumber(redis.call('HGET', KEYS[1], 'doctorOfferExpiresAt'))
            if tonumber(ARGV[3]) >= deadline then return -2 end
            redis.call('HSET', KEYS[1], 'state', 'WAITING_MEMBER_CONFIRMATION',
              'doctorAcceptedAt', ARGV[3], 'memberConfirmExpiresAt', ARGV[4])
            redis.call('PEXPIRE', KEYS[1], ARGV[5])
            redis.call('PEXPIRE', KEYS[2], ARGV[5])
            redis.call('PEXPIRE', KEYS[3], ARGV[5])
            redis.call('ZREM', KEYS[4], ARGV[1])
            redis.call('ZADD', KEYS[5], ARGV[4], ARGV[1])
            return 1
            """, Long.class);

    private static final DefaultRedisScript<Long> RELEASE = new DefaultRedisScript<>("""
            local removed = 0
            if redis.call('GET', KEYS[2]) == ARGV[1] then redis.call('DEL', KEYS[2]); removed = 1 end
            if redis.call('GET', KEYS[3]) == ARGV[1] then redis.call('DEL', KEYS[3]); removed = 1 end
            if redis.call('HGET', KEYS[1], 'offerId') == ARGV[1] then redis.call('DEL', KEYS[1]); removed = 1 end
            redis.call('ZREM', KEYS[4], ARGV[1])
            redis.call('ZREM', KEYS[5], ARGV[1])
            return removed
            """, Long.class);

    private static final DefaultRedisScript<Long> CONFIRM = new DefaultRedisScript<>("""
            if redis.call('GET', KEYS[2]) ~= ARGV[1] or redis.call('GET', KEYS[3]) ~= ARGV[1] then return -1 end
            if redis.call('HGET', KEYS[1], 'offerId') ~= ARGV[1]
              or redis.call('HGET', KEYS[1], 'queueEntryId') ~= ARGV[2]
              or redis.call('HGET', KEYS[1], 'memberId') ~= ARGV[3]
              or redis.call('HGET', KEYS[1], 'doctorId') ~= ARGV[4] then return -1 end
            local state = redis.call('HGET', KEYS[1], 'state')
            local deadline = tonumber(redis.call('HGET', KEYS[1], 'memberConfirmExpiresAt'))
            if deadline == nil or tonumber(ARGV[5]) >= deadline then return -2 end
            if state == 'CONFIRMED' then return 2 end
            if state ~= 'WAITING_MEMBER_CONFIRMATION' then return -1 end
            redis.call('HSET', KEYS[1], 'state', 'CONFIRMED')
            return 1
            """, Long.class);

    private static final DefaultRedisScript<Long> RESTORE_CONFIRMATION = new DefaultRedisScript<>("""
            if redis.call('GET', KEYS[2]) ~= ARGV[1] or redis.call('GET', KEYS[3]) ~= ARGV[1] then return 0 end
            if redis.call('HGET', KEYS[1], 'offerId') ~= ARGV[1]
              or redis.call('HGET', KEYS[1], 'state') ~= 'CONFIRMED' then return 0 end
            redis.call('HSET', KEYS[1], 'state', 'WAITING_MEMBER_CONFIRMATION')
            redis.call('ZADD', KEYS[4], ARGV[2], ARGV[1])
            redis.call('PEXPIRE', KEYS[1], ARGV[3])
            redis.call('PEXPIRE', KEYS[2], ARGV[3])
            redis.call('PEXPIRE', KEYS[3], ARGV[3])
            return 1
            """, Long.class);

    private final StringRedisTemplate redis;

    @Value("${app.consultation.dispatch.offer-cleanup-ttl-minutes:30}")
    long cleanupTtlMinutes;

    @Override
    public CreateResult create(DoctorOffer o) {
        Long result = execute(CREATE, List.of(offerKey(o.offerId()), doctorKey(o.doctorId()),
                        queueKey(o.queueEntryId()), DOCTOR_DEADLINES),
                o.offerId(), s(o.queueEntryId()), s(o.requestId()), s(o.memberId()), s(o.doctorId()),
                o.state().name(), ms(o.offeredAt()), ms(o.doctorOfferExpiresAt()), ttl(), ms(o.doctorOfferExpiresAt()));
        return result == 1 ? CreateResult.CREATED : result == 2 ? CreateResult.DOCTOR_ALREADY_HELD
                : CreateResult.QUEUE_ALREADY_HELD;
    }

    @Override public Optional<DoctorOffer> findById(String offerId) { return read(offerId); }
    @Override public Optional<DoctorOffer> findByDoctorId(Long doctorId) {
        String id = safe(() -> redis.opsForValue().get(doctorKey(doctorId)));
        return id == null ? Optional.empty() : read(id);
    }
    @Override public Optional<DoctorOffer> findByQueueEntryId(Long queueEntryId) {
        String id = safe(() -> redis.opsForValue().get(queueKey(queueEntryId)));
        return id == null ? Optional.empty() : read(id);
    }

    @Override
    public AcceptResult accept(String offerId, Long doctorId, Instant now, Instant memberDeadline) {
        DoctorOffer current = read(offerId).orElse(null);
        if (current == null || !current.doctorId().equals(doctorId)) return AcceptResult.STALE;
        Long result = execute(ACCEPT, List.of(offerKey(offerId), doctorKey(doctorId),
                        queueKey(current.queueEntryId()), DOCTOR_DEADLINES, MEMBER_DEADLINES),
                offerId, s(doctorId), ms(now), ms(memberDeadline), ttl());
        return result == 1 ? AcceptResult.ACCEPTED : result == 2 ? AcceptResult.ALREADY_ACCEPTED
                : result == -2 ? AcceptResult.EXPIRED : AcceptResult.STALE;
    }

    @Override
    public ConfirmResult confirm(String offerId, Long queueEntryId, Long memberId, Long doctorId, Instant now) {
        Long result = execute(CONFIRM, List.of(offerKey(offerId), doctorKey(doctorId),
                        queueKey(queueEntryId), MEMBER_DEADLINES),
                offerId, s(queueEntryId), s(memberId), s(doctorId), ms(now));
        return result == 1 ? ConfirmResult.CONFIRMED : result == 2 ? ConfirmResult.ALREADY_CONFIRMED
                : result == -2 ? ConfirmResult.EXPIRED : ConfirmResult.STALE;
    }

    @Override
    public boolean restoreMemberConfirmation(DoctorOffer offer) {
        if (offer.memberConfirmExpiresAt() == null) return false;
        return execute(RESTORE_CONFIRMATION, List.of(offerKey(offer.offerId()), doctorKey(offer.doctorId()),
                        queueKey(offer.queueEntryId()), MEMBER_DEADLINES),
                offer.offerId(), ms(offer.memberConfirmExpiresAt()), ttl()) == 1;
    }

    @Override public boolean release(DoctorOffer o) {
        return execute(RELEASE, List.of(offerKey(o.offerId()), doctorKey(o.doctorId()),
                queueKey(o.queueEntryId()), DOCTOR_DEADLINES, MEMBER_DEADLINES), o.offerId()) > 0;
    }

    @Override public List<String> dueDoctorOfferIds(Instant now, int limit) { return due(DOCTOR_DEADLINES, now, limit); }
    @Override public List<String> dueMemberConfirmationOfferIds(Instant now, int limit) { return due(MEMBER_DEADLINES, now, limit); }

    @Override
    public Set<Long> heldDoctorIds(Collection<Long> ids) {
        if (ids.isEmpty()) return Set.of();
        List<Long> ordered = new ArrayList<>(ids);
        List<String> values = safe(() -> redis.opsForValue().multiGet(ordered.stream().map(this::doctorKey).toList()));
        if (values == null) throw unavailable(null);
        Set<Long> held = new HashSet<>();
        for (int i = 0; i < ordered.size(); i++) if (values.get(i) != null) held.add(ordered.get(i));
        return held;
    }

    private Optional<DoctorOffer> read(String offerId) {
        Map<Object,Object> h = safe(() -> redis.opsForHash().entries(offerKey(offerId)));
        if (h == null || h.isEmpty()) return Optional.empty();
        return Optional.of(new DoctorOffer(str(h,"offerId"), lng(h,"queueEntryId"), lng(h,"requestId"),
                lng(h,"memberId"), lng(h,"doctorId"), DoctorOfferState.valueOf(str(h,"state")),
                instant(h,"offeredAt"), instant(h,"doctorOfferExpiresAt"),
                instantOrNull(h,"doctorAcceptedAt"), instantOrNull(h,"memberConfirmExpiresAt")));
    }

    private List<String> due(String key, Instant now, int limit) {
        Set<String> values = safe(() -> redis.opsForZSet().rangeByScore(key, 0, now.toEpochMilli(), 0, limit));
        if (values == null || values.isEmpty()) return List.of();
        List<String> active = new ArrayList<>();
        for (String offerId : values) {
            if (safe(() -> Boolean.TRUE.equals(redis.hasKey(offerKey(offerId))))) active.add(offerId);
            else safe(() -> redis.opsForZSet().remove(key, offerId));
        }
        return active;
    }
    private Long execute(DefaultRedisScript<Long> script, List<String> keys, String... args) {
        Long result = safe(() -> redis.execute(script, keys, (Object[]) args));
        if (result == null) throw unavailable(null);
        return result;
    }
    private <T> T safe(SupplierWithException<T> action) {
        try { return action.get(); }
        catch (RuntimeException ex) { throw unavailable(ex); }
    }
    private AppException unavailable(Throwable cause) {
        return new AppException(ErrorCode.DISPATCH_TEMPORARILY_UNAVAILABLE,
                cause == null ? "Redis offer state is unavailable" : "Redis offer state is unavailable: " + cause.getClass().getSimpleName());
    }
    private String offerKey(String id) { return OFFER_PREFIX + id; }
    private String doctorKey(Long id) { return DOCTOR_PREFIX + id; }
    private String queueKey(Long id) { return QUEUE_PREFIX + id; }
    private String ttl() { return Long.toString(Duration.ofMinutes(cleanupTtlMinutes).toMillis()); }
    private String ms(Instant i) { return Long.toString(i.toEpochMilli()); }
    private String s(Long value) { return value.toString(); }
    private String str(Map<Object,Object> h, String k) { return Objects.toString(h.get(k)); }
    private Long lng(Map<Object,Object> h, String k) { return Long.valueOf(str(h,k)); }
    private Instant instant(Map<Object,Object> h, String k) { return Instant.ofEpochMilli(Long.parseLong(str(h,k))); }
    private Instant instantOrNull(Map<Object,Object> h, String k) { return h.get(k) == null ? null : instant(h,k); }
    @FunctionalInterface private interface SupplierWithException<T> { T get(); }
}
