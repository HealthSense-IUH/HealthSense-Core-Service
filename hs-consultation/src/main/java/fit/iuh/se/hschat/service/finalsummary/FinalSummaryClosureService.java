package fit.iuh.se.hschat.service.finalsummary;

import fit.iuh.se.hschat.entity.ConsultationSession;

import java.time.Instant;

public interface FinalSummaryClosureService {

    void onSessionCompleted(ConsultationSession session, Instant completedAt);

    void onSummaryFinalized(ConsultationSession session, Instant finalizedAt);

    void refreshOpenClosures(Instant now);
}
