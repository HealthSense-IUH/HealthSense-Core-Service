package fit.iuh.se.hschat.service.continuation;

import fit.iuh.se.hschat.dto.request.SubmitContinuationDecisionRequest;
import fit.iuh.se.hschat.dto.response.ContinuationDecisionResponse;
import fit.iuh.se.hsuser.entity.enums.UserRole;

import java.time.Instant;
import java.util.List;

public interface QueueContinuationService {

    ContinuationDecisionResponse getCurrent(Long actorId, UserRole actorRole, Long sessionId);

    ContinuationDecisionResponse decide(Long actorId, UserRole actorRole, Long sessionId,
            int round, SubmitContinuationDecisionRequest request);

    List<Long> findDueSessionIds(Instant now, int limit);

    void processDeadline(Long sessionId, Instant now);
}
