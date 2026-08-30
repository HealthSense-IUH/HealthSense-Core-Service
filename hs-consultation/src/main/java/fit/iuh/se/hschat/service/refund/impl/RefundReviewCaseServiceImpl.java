package fit.iuh.se.hschat.service.refund.impl;

import fit.iuh.se.hschat.entity.*;
import fit.iuh.se.hschat.entity.enums.ConsultationRefundStatus;
import fit.iuh.se.hschat.repository.*;
import fit.iuh.se.hschat.service.refund.RefundReviewCaseService;
import fit.iuh.se.hsoperations.dto.command.*;
import fit.iuh.se.hsoperations.entity.enums.*;
import fit.iuh.se.hsoperations.service.OperationalEventService;
import fit.iuh.se.hsuser.entity.enums.UserRole;
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
    OperationalEventService operationalEventService;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void ensureReviewRequired(ConsultationPayment payment) {
        if (payment.getId() == null || refundRepository.findByPaymentId(payment.getId()).isPresent()) return;
        CareServiceAgreement agreement = agreementRepository.findById(payment.getAgreementId()).orElse(null);
        if (agreement == null) return;
        ConsultationRefund refund = refundRepository.save(ConsultationRefund.builder()
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
        operationalEventService.record(OperationalEventCommand.builder()
                .domainType(BusinessDomainType.REFUND).domainId(refund.getId())
                .eventType(BusinessEventType.REFUND_REVIEW_REQUIRED).actorType(BusinessActorType.SYSTEM)
                .requestId(refund.getRequestId()).paymentId(refund.getPaymentId()).refundId(refund.getId())
                .renewalId(refund.getRenewalId()).sessionId(refund.getSessionId()).memberId(refund.getMemberId())
                .newState(ConsultationRefundStatus.REVIEW_REQUIRED.name())
                .idempotencyKey("refund:" + refund.getId() + ":review-required")
                .needsAction(new NeedsActionIntent(NeedsActionType.REFUND_REVIEW_REQUIRED, NeedsActionPriority.HIGH,
                        "Refund review required", "Paid evidence requires a refund recommendation.",
                        BusinessDomainType.REFUND, refund.getId(), UserRole.CARE_COORDINATOR.name(),
                        "refund:" + refund.getId() + ":review-required"))
                .notifications(java.util.List.of(NotificationIntent.forRole(UserRole.CARE_COORDINATOR,
                        NotificationType.OPERATIONAL_REVIEW_REQUIRED, "Refund review required",
                        "Paid evidence requires a refund recommendation.", BusinessDomainType.REFUND,
                        refund.getId(), "refund:" + refund.getId() + ":review-required:coordinators")))
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
