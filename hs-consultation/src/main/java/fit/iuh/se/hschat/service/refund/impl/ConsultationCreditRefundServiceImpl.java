package fit.iuh.se.hschat.service.refund.impl;

import fit.iuh.se.hsbilling.dto.CreditMutationResponse;
import fit.iuh.se.hsbilling.service.CreditAdministrationService;
import fit.iuh.se.hschat.entity.enums.ConsultationCreditPolicy;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hschat.service.refund.ConsultationCreditRefundService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConsultationCreditRefundServiceImpl implements ConsultationCreditRefundService {
    private final ConsultationSessionRepository sessions;
    private final CreditAdministrationService credits;

    @Override
    @Transactional
    public CreditMutationResponse refund(Long actorId, UserRole role, Long sessionId, String reason, String key) {
        if (role != UserRole.ADMIN && role != UserRole.SUPER_ADMIN) throw new AppException(ErrorCode.ACCESS_DENIED);
        if (sessionId == null || sessionId <= 0) throw new AppException(ErrorCode.INVALID_PARAMETER);
        var session = sessions.findById(sessionId).orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_NOT_FOUND));
        if ((session.getCreditPolicy() != ConsultationCreditPolicy.PER_SESSION_V1
                && session.getCreditPolicy() != ConsultationCreditPolicy.PER_SESSION_CONFIRM_V2)
                || session.getCreditCost() == null || session.getCreditCost() <= 0)
            throw new AppException(ErrorCode.CREDIT_REFUND_NOT_ELIGIBLE,
                    "Free sessions and sessions without a captured charge cannot be refunded");
        return credits.refundCapturedSession(actorId, role, session.getMemberId(), session.getId(),
                session.getCreditCost(), reason, key);
    }
}
