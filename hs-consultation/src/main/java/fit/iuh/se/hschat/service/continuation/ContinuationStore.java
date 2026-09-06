package fit.iuh.se.hschat.service.continuation;

import fit.iuh.se.hschat.entity.enums.ContinuationDecision;
import fit.iuh.se.hsuser.entity.enums.UserRole;

import java.time.Instant;
import java.util.Optional;

public interface ContinuationStore {

    enum OpenResult { CREATED, ALREADY_OPEN, STALE }
    enum DecisionResult { ACCEPTED, IDEMPOTENT, CONFLICT, EXPIRED, STALE }

    OpenResult open(Long sessionId, int round, Instant promptedAt, Instant graceExpiresAt);

    Optional<ContinuationState> find(Long sessionId, int round);

    DecisionOutcome decide(Long sessionId, int round, UserRole actorRole,
            ContinuationDecision decision, Instant now);

    boolean release(Long sessionId, int round);

    record DecisionOutcome(DecisionResult result, ContinuationState state) {
    }
}
