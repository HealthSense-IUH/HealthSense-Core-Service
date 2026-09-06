package fit.iuh.se.hschat.service.session;

import fit.iuh.se.hschat.dto.response.ConsultationSessionResponse;

public interface QueueConsultationSessionService {
    ConsultationSessionResponse confirmMember(Long memberId, Long requestId, String offerId);
}
