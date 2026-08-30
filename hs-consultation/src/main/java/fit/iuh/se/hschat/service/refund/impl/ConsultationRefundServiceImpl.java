package fit.iuh.se.hschat.service.refund.impl;

import fit.iuh.se.hschat.dto.ProviderRefundResult;
import fit.iuh.se.hschat.dto.request.*;
import fit.iuh.se.hschat.dto.response.ConsultationRefundResponse;
import fit.iuh.se.hschat.entity.*;
import fit.iuh.se.hschat.entity.enums.*;
import fit.iuh.se.hschat.repository.*;
import fit.iuh.se.hschat.service.payment.PayOSPaymentGateway;
import fit.iuh.se.hschat.service.refund.ConsultationRefundService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsoperations.dto.command.*;
import fit.iuh.se.hsoperations.entity.enums.*;
import fit.iuh.se.hsoperations.service.OperationalEventService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConsultationRefundServiceImpl implements ConsultationRefundService {

    ConsultationRefundRepository refundRepository;
    ConsultationPaymentRepository paymentRepository;
    CareServiceAgreementRepository agreementRepository;
    ConsultationRequestRepository requestRepository;
    ConsultationRenewalRepository renewalRepository;
    PayOSPaymentGateway paymentGateway;
    OperationalEventService operationalEventService;

    @Override
    @Transactional
    public ConsultationRefundResponse recommend(
            Long actorId, UserRole role, Long paymentId, RecommendRefundRequest request) {
        requireCoordinator(role);
        ConsultationPayment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_PAYMENT_NOT_FOUND));
        requirePaidEvidence(payment);
        CareServiceAgreement agreement = agreementRepository.findById(payment.getAgreementId())
                .orElseThrow(() -> new AppException(ErrorCode.ENTITY_NOT_FOUND, "Agreement not found"));

        ConsultationRefund refund = refundRepository.findByPaymentIdForUpdate(paymentId)
                .orElseGet(() -> ConsultationRefund.builder()
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
        if (refund.getStatus() != ConsultationRefundStatus.REVIEW_REQUIRED
                && refund.getStatus() != ConsultationRefundStatus.RECOMMENDED)
            throw new AppException(ErrorCode.INVALID_REFUND_STATUS);

        BigDecimal recommendedAmount = normalizeRecommendationAmount(
                request.getRecommendation(), request.getRecommendedAmount(), payment.getAmount());
        refund.setRecommendation(request.getRecommendation());
        refund.setRecommendedAmount(recommendedAmount);
        refund.setReviewReason(request.getReason().trim());
        refund.setOperationalContext(trimToNull(request.getOperationalContext()));
        refund.setReviewedBy(actorId);
        refund.setReviewedAt(Instant.now());
        refund.setStatus(ConsultationRefundStatus.RECOMMENDED);
        refund = refundRepository.save(refund);
        auditRefund(refund, BusinessEventType.REFUND_RECOMMENDED, actorId, role,
                ConsultationRefundStatus.REVIEW_REQUIRED, ConsultationRefundStatus.RECOMMENDED, request.getReason(), null);
        return toResponse(refund);
    }

    @Override
    @Transactional
    public ConsultationRefundResponse decide(
            Long actorId, UserRole role, Long refundId, DecideRefundRequest request) {
        requireFinancialAdmin(role);
        ConsultationRefund refund = requireLocked(refundId);
        if (refund.getStatus() == ConsultationRefundStatus.APPROVED) {
            if (Boolean.TRUE.equals(request.getApproved())
                    && refund.getApprovedAmount().compareTo(request.getApprovedAmount()) == 0)
                return toResponse(refund);
            throw new AppException(ErrorCode.INVALID_REFUND_STATUS,
                    "Final refund decision cannot be changed");
        }
        if (refund.getStatus() == ConsultationRefundStatus.REJECTED) {
            if (!Boolean.TRUE.equals(request.getApproved())) return toResponse(refund);
            throw new AppException(ErrorCode.INVALID_REFUND_STATUS,
                    "Final refund decision cannot be changed");
        }
        if (refund.getStatus() != ConsultationRefundStatus.RECOMMENDED)
            throw new AppException(ErrorCode.INVALID_REFUND_STATUS);

        Instant now = Instant.now();
        refund.setDecidedBy(actorId);
        refund.setDecidedAt(now);
        refund.setDecisionReason(request.getReason().trim());
        if (Boolean.TRUE.equals(request.getApproved())) {
            BigDecimal approvedAmount = requireValidAmount(request.getApprovedAmount(), refund.getOriginalPaidAmount());
            refund.setApprovedAmount(approvedAmount);
            refund.setStatus(ConsultationRefundStatus.APPROVED);
        } else {
            refund.setApprovedAmount(null);
            refund.setStatus(ConsultationRefundStatus.REJECTED);
        }
        refund = refundRepository.save(refund);
        auditRefund(refund, Boolean.TRUE.equals(request.getApproved()) ? BusinessEventType.REFUND_APPROVED : BusinessEventType.REFUND_REJECTED,
                actorId, role, ConsultationRefundStatus.RECOMMENDED, refund.getStatus(), request.getReason(), null);
        return toResponse(refund);
    }

    @Override
    @Transactional
    public ConsultationRefundResponse execute(Long actorId, UserRole role, Long refundId) {
        requireFinancialAdmin(role);
        ConsultationRefund refund = requireLocked(refundId);
        if (refund.getStatus() == ConsultationRefundStatus.SUCCEEDED)
            return toResponse(refund);
        if (refund.getStatus() != ConsultationRefundStatus.APPROVED
                && refund.getStatus() != ConsultationRefundStatus.FAILED)
            throw new AppException(ErrorCode.INVALID_REFUND_STATUS);

        ConsultationPayment payment = paymentRepository.findById(refund.getPaymentId())
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_PAYMENT_NOT_FOUND));
        Instant now = Instant.now();
        refund.setStatus(ConsultationRefundStatus.PROCESSING);
        refund.setExecutionAttempts(refund.getExecutionAttempts() + 1);
        refund.setLastExecutionAt(now);
        refundRepository.saveAndFlush(refund);
        try {
            ProviderRefundResult result = paymentGateway.refundPayment(
                    payment.getOrderCode(), refund.getApprovedAmount(), refund.getCurrency(),
                    "consultation-refund-" + refund.getId(), refund.getDecisionReason());
            refund.setStatus(ConsultationRefundStatus.SUCCEEDED);
            refund.setProviderRefundId(result == null ? null : result.getProviderRefundId());
            refund.setProviderResult(result == null ? "Provider accepted refund" : result.getResult());
            refund.setCompletedAt(now);
        } catch (Exception exception) {
            refund.setStatus(ConsultationRefundStatus.FAILED);
            refund.setProviderResult(truncate(exception.getMessage()));
        }
        refund = refundRepository.save(refund);
        NeedsActionIntent failure = refund.getStatus() == ConsultationRefundStatus.FAILED
                ? refundFailureAction(refund) : null;
        auditRefund(refund, refund.getStatus() == ConsultationRefundStatus.SUCCEEDED
                        ? BusinessEventType.REFUND_SUCCEEDED : BusinessEventType.REFUND_FAILED,
                actorId, role, ConsultationRefundStatus.PROCESSING, refund.getStatus(), refund.getProviderResult(), failure);
        return toResponse(refund);
    }

    @Override
    @Transactional
    public ConsultationRefundResponse reconcile(
            Long actorId, UserRole role, Long refundId, ReconcileRefundRequest request) {
        requireFinancialAdmin(role);
        ConsultationRefund refund = requireLocked(refundId);
        if (refund.getStatus() == ConsultationRefundStatus.REJECTED)
            throw new AppException(ErrorCode.INVALID_REFUND_STATUS);
        if (refund.getStatus() == ConsultationRefundStatus.SUCCEEDED)
            return toResponse(refund);
        refund.setProviderRefundId(trimToNull(request.getProviderRefundId()));
        refund.setProviderResult(request.getProviderResult().trim());
        refund.setStatus(Boolean.TRUE.equals(request.getSucceeded())
                ? ConsultationRefundStatus.SUCCEEDED : ConsultationRefundStatus.FAILED);
        refund.setCompletedAt(Boolean.TRUE.equals(request.getSucceeded()) ? Instant.now() : null);
        refund = refundRepository.save(refund);
        auditRefund(refund, BusinessEventType.REFUND_RECONCILED, actorId, role, null, refund.getStatus(),
                request.getProviderResult(), refund.getStatus() == ConsultationRefundStatus.FAILED ? refundFailureAction(refund) : null);
        return toResponse(refund);
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultationRefundResponse get(UserRole role, Long refundId) {
        requireRefundStaff(role);
        return toResponse(refundRepository.findById(refundId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_REFUND_NOT_FOUND)));
    }

    private ConsultationRefund requireLocked(Long refundId) {
        return refundRepository.findByIdForUpdate(refundId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_REFUND_NOT_FOUND));
    }

    private void requirePaidEvidence(ConsultationPayment payment) {
        boolean paid = payment.getStatus() == ConsultationPaymentStatus.PAID
                || (payment.getStatus() == ConsultationPaymentStatus.REQUIRES_REVIEW && payment.getPaidAt() != null);
        if (!paid)
            throw new AppException(ErrorCode.INVALID_CONSULTATION_PAYMENT_STATUS,
                    "Refund review requires verified paid evidence");
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

    private BigDecimal normalizeRecommendationAmount(
            RefundRecommendation recommendation, BigDecimal amount, BigDecimal paidAmount) {
        return switch (recommendation) {
            case FULL -> paidAmount;
            case NONE -> null;
            case PARTIAL -> requireValidAmount(amount, paidAmount);
        };
    }

    private BigDecimal requireValidAmount(BigDecimal amount, BigDecimal paidAmount) {
        if (amount == null || amount.signum() <= 0 || amount.compareTo(paidAmount) > 0)
            throw new AppException(ErrorCode.INVALID_PARAMETER,
                    "Refund amount must be positive and cannot exceed the original paid amount");
        return amount;
    }

    private void requireCoordinator(UserRole role) {
        if (role != UserRole.CARE_COORDINATOR)
            throw new AppException(ErrorCode.ACCESS_DENIED,
                    "Only a Care Coordinator may recommend a refund");
    }

    private void requireFinancialAdmin(UserRole role) {
        if (role != UserRole.ADMIN && role != UserRole.SUPER_ADMIN)
            throw new AppException(ErrorCode.ACCESS_DENIED,
                    "Only Admin or Super Admin may decide or execute a refund");
    }

    private void requireRefundStaff(UserRole role) {
        if (role != UserRole.CARE_COORDINATOR && role != UserRole.ADMIN && role != UserRole.SUPER_ADMIN)
            throw new AppException(ErrorCode.ACCESS_DENIED);
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String truncate(String value) {
        if (value == null) return "Unknown refund provider failure";
        return value.length() <= 1000 ? value : value.substring(0, 1000);
    }

    private NeedsActionIntent refundFailureAction(ConsultationRefund refund) {
        return new NeedsActionIntent(NeedsActionType.REFUND_PROVIDER_FAILURE, NeedsActionPriority.CRITICAL,
                "Refund provider failure", "The approved refund requires provider reconciliation.",
                BusinessDomainType.REFUND, refund.getId(), UserRole.ADMIN.name(),
                "refund:" + refund.getId() + ":provider-failure");
    }

    private void auditRefund(ConsultationRefund refund, BusinessEventType eventType, Long actorId, UserRole actorRole,
            ConsultationRefundStatus previous, ConsultationRefundStatus next, String reason, NeedsActionIntent needsAction) {
        String key = "refund:" + refund.getId() + ":" + eventType + ":" + refund.getExecutionAttempts();
        operationalEventService.record(OperationalEventCommand.builder()
                .domainType(BusinessDomainType.REFUND).domainId(refund.getId()).eventType(eventType)
                .actorType(BusinessActorType.USER).actorUserId(actorId).actorRole(actorRole.name())
                .requestId(refund.getRequestId()).paymentId(refund.getPaymentId()).agreementId(refund.getAgreementId())
                .sessionId(refund.getSessionId()).renewalId(refund.getRenewalId()).refundId(refund.getId())
                .memberId(refund.getMemberId()).previousState(previous == null ? null : previous.name())
                .newState(next == null ? null : next.name()).reason(reason).idempotencyKey(key).needsAction(needsAction)
                .notifications(java.util.List.of(new NotificationIntent(refund.getMemberId(), NotificationType.REFUND_STATUS_CHANGED,
                        "Refund status updated", "Your refund status is now " + next + ".",
                        BusinessDomainType.REFUND, refund.getId(), key + ":member")))
                .build());
    }

    private ConsultationRefundResponse toResponse(ConsultationRefund refund) {
        return ConsultationRefundResponse.builder()
                .id(refund.getId()).paymentId(refund.getPaymentId()).requestId(refund.getRequestId())
                .renewalId(refund.getRenewalId()).sessionId(refund.getSessionId())
                .agreementId(refund.getAgreementId()).memberId(refund.getMemberId())
                .originalPaidAmount(refund.getOriginalPaidAmount()).currency(refund.getCurrency())
                .refundPolicyReference(refund.getRefundPolicyReference()).status(refund.getStatus())
                .recommendation(refund.getRecommendation()).recommendedAmount(refund.getRecommendedAmount())
                .reviewReason(refund.getReviewReason()).operationalContext(refund.getOperationalContext())
                .reviewedBy(refund.getReviewedBy()).reviewedAt(refund.getReviewedAt())
                .approvedAmount(refund.getApprovedAmount()).decisionReason(refund.getDecisionReason())
                .decidedBy(refund.getDecidedBy()).decidedAt(refund.getDecidedAt())
                .provider(refund.getProvider()).providerRefundId(refund.getProviderRefundId())
                .providerResult(refund.getProviderResult()).executionAttempts(refund.getExecutionAttempts())
                .lastExecutionAt(refund.getLastExecutionAt()).completedAt(refund.getCompletedAt())
                .createdAt(refund.getCreatedAt()).updatedAt(refund.getUpdatedAt()).build();
    }
}
