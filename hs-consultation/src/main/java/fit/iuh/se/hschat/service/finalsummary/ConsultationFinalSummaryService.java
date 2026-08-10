package fit.iuh.se.hschat.service.finalsummary;

import fit.iuh.se.hschat.dto.request.UpsertConsultationFinalSummaryRequest;
import fit.iuh.se.hschat.dto.response.ConsultationFinalSummaryResponse;
import fit.iuh.se.hsuser.entity.enums.UserRole;

public interface ConsultationFinalSummaryService {

    ConsultationFinalSummaryResponse getForDoctor(Long doctorId, Long sessionId);

    ConsultationFinalSummaryResponse upsertDraft(Long doctorId, Long sessionId, UpsertConsultationFinalSummaryRequest request);

    ConsultationFinalSummaryResponse finalizeSummary(Long doctorId, Long sessionId);

    ConsultationFinalSummaryResponse getForMember(Long memberId, Long sessionId);

    ConsultationFinalSummaryResponse getForAdmin(UserRole actorRole, Long sessionId);
}
