package fit.iuh.se.hschat.service.refund;

import fit.iuh.se.hsbilling.dto.CreditMutationResponse;
import fit.iuh.se.hsuser.entity.enums.UserRole;

public interface ConsultationCreditRefundService {
    CreditMutationResponse refund(Long actorId, UserRole role, Long sessionId, String reason, String idempotencyKey);
}
