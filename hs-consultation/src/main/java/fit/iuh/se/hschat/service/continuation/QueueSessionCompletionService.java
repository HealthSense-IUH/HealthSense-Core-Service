package fit.iuh.se.hschat.service.continuation;

import fit.iuh.se.hschat.dto.response.ContinuationDecisionResponse;
import fit.iuh.se.hschat.entity.enums.ConsultationCompletionReason;

import java.time.Instant;

public interface QueueSessionCompletionService {

    ContinuationDecisionResponse complete(
            Long sessionId, int expectedRound, Instant completedAt, ConsultationCompletionReason reason);
}
