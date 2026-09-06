package fit.iuh.se.hschat.dto.response;

import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import fit.iuh.se.hschat.entity.enums.ContinuationDecision;

import java.time.Instant;

public record ContinuationDecisionResponse(
        Long sessionId,
        Integer round,
        ContinuationDecision doctorDecision,
        ContinuationDecision memberDecision,
        Instant promptedAt,
        Instant graceExpiresAt,
        ConsultationStatus sessionStatus,
        Instant blockStartedAt,
        Instant endsAt,
        Integer continuationRound) {
}
