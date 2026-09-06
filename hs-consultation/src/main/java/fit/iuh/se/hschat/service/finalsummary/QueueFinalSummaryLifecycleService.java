package fit.iuh.se.hschat.service.finalsummary;

import fit.iuh.se.hschat.entity.ConsultationFinalSummary;
import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.DoctorCareProfile;
import fit.iuh.se.hsuser.entity.UserAccount;

import java.time.Instant;
import java.util.List;

public interface QueueFinalSummaryLifecycleService {

    void onSummaryFinalized(
            ConsultationSession session,
            DoctorCareProfile profile,
            UserAccount doctor,
            ConsultationFinalSummary summary,
            Instant now);

    List<Long> findDueSessionIds(Instant now, int limit);

    void processDeadline(Long sessionId, Instant now);
}
