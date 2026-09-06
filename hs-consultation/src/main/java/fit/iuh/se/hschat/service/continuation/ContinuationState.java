package fit.iuh.se.hschat.service.continuation;

import fit.iuh.se.hschat.entity.enums.ContinuationDecision;

import java.time.Instant;

public record ContinuationState(
        Long sessionId,
        int round,
        ContinuationDecision doctorDecision,
        ContinuationDecision memberDecision,
        Instant promptedAt,
        Instant graceExpiresAt) {

    public boolean bothContinue() {
        return doctorDecision == ContinuationDecision.CONTINUE
                && memberDecision == ContinuationDecision.CONTINUE;
    }
}
