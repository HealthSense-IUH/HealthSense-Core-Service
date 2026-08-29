package fit.iuh.se.hschat.service.refund.impl;

import fit.iuh.se.hschat.entity.*;
import fit.iuh.se.hschat.entity.enums.ConsultationRefundStatus;
import fit.iuh.se.hschat.repository.*;
import fit.iuh.se.hschat.service.refund.RefundReviewCaseService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RefundReviewCaseServiceImpl implements RefundReviewCaseService {
    ConsultationRefundRepository refundRepository;
    CareServiceAgreementRepository agreementRepository;
    ConsultationRequestRepository requestRepository;
    ConsultationRenewalRepository renewalRepository;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void ensureReviewRequired(ConsultationPayment payment) {
        if (payment.getId() == null || refundRepository.findByPaymentId(payment.getId()).isPresent()) return;
        CareServiceAgreement agreement = agreementRepository.findById(payment.getAgreementId()).orElse(null);
        if (agreement == null) return;
        refundRepository.save(ConsultationRefund.builder()
                .paymentId(payment.getId())
                .requestId(payment.getRequestId())
                .renewalId(payment.getRenewalId())
                .sessionId(resolveSessionId(payment))
                .agreementId(payment.getAgreementId())
                .memberId(payment.getMemberId())
                .originalPaidAmount(payment.getAmount())
                .currency(payment.getCurrency())
                .refundPolicyReference(agreement.getRefundPolicyReference())
                .provider(payment.getProvider())
                .status(ConsultationRefundStatus.REVIEW_REQUIRED)
                .build());
    }

    private Long resolveSessionId(ConsultationPayment payment) {
        if (payment.getRequestId() != null)
            return requestRepository.findById(payment.getRequestId())
                    .map(ConsultationRequest::getConsultationSessionId).orElse(null);
        if (payment.getRenewalId() != null)
            return renewalRepository.findById(payment.getRenewalId())
                    .map(ConsultationRenewal::getSessionId).orElse(null);
        return null;
    }
}
