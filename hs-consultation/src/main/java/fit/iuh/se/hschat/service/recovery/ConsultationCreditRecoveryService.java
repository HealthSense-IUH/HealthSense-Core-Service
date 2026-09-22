package fit.iuh.se.hschat.service.recovery;

import fit.iuh.se.hschat.dto.response.CreditRecoveryResponse;
import fit.iuh.se.hsuser.entity.enums.UserRole;

public interface ConsultationCreditRecoveryService {
    CreditRecoveryResponse recover(Long actorId, UserRole role, int limit);
}
