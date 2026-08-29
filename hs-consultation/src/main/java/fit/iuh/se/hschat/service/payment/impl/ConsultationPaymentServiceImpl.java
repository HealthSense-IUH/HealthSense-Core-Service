package fit.iuh.se.hschat.service.payment.impl;

import fit.iuh.se.hschat.dto.PayOSPaymentLink;
import fit.iuh.se.hschat.dto.VerifiedPayOSPayment;
import fit.iuh.se.hschat.dto.response.ConsultationPaymentResponse;
import fit.iuh.se.hschat.entity.ConsultationParticipant;
import fit.iuh.se.hschat.entity.ConsultationPayment;
import fit.iuh.se.hschat.entity.ConsultationRequest;
import fit.iuh.se.hschat.entity.ConsultationRenewal;
import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.CareServiceAgreement;
import fit.iuh.se.hschat.entity.enums.*;
import fit.iuh.se.hschat.repository.ConsultationParticipantRepository;
import fit.iuh.se.hschat.repository.ConsultationPaymentRepository;
import fit.iuh.se.hschat.repository.ConsultationRequestRepository;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hschat.service.payment.ConsultationPaymentService;
import fit.iuh.se.hschat.service.payment.PayOSPaymentGateway;
import fit.iuh.se.hschat.service.agreement.CareServiceAgreementService;
import fit.iuh.se.hschat.service.authorization.EpisodeHealthRecordAuthorizationService;
import fit.iuh.se.hschat.service.reservation.DoctorReservationInvalidException;
import fit.iuh.se.hschat.service.reservation.DoctorReservationService;
import fit.iuh.se.hschat.service.renewal.ConsultationRenewalService;
import fit.iuh.se.hschat.service.refund.RefundReviewCaseService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.model.webhooks.Webhook;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConsultationPaymentServiceImpl implements ConsultationPaymentService {

    static final List<ConsultationPaymentStatus> EXPIRABLE_PAYMENT_STATUSES = List.of(
            ConsultationPaymentStatus.PENDING,
            ConsultationPaymentStatus.FAILED
    );
    static final long MIN_HEALTHSENSE_ORDER_CODE = 1_000_000_000_000_000L;

    ConsultationPaymentRepository paymentRepository;
    ConsultationRequestRepository requestRepository;
    ConsultationSessionRepository sessionRepository;
    ConsultationParticipantRepository participantRepository;
    PayOSPaymentGateway paymentGateway;
    DoctorReservationService reservationService;
    CareServiceAgreementService agreementService;
    EpisodeHealthRecordAuthorizationService authorizationService;
    ConsultationRenewalService renewalService;
    RefundReviewCaseService refundReviewCaseService;

    @NonFinal
    @Value("${app.payment.return-url:http://localhost:5173/payment/result}")
    String returnUrl;

    @NonFinal
    @Value("${app.payment.cancel-url:http://localhost:5173/payment/cancel}")
    String cancelUrl;

    @Override
    @Transactional(noRollbackFor = DoctorReservationInvalidException.class)
    public ConsultationPaymentResponse createPayment(Long memberId, Long requestId) {
        ConsultationRequest request = requestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_REQUEST_NOT_FOUND));
        validateMemberOwnsRequest(memberId, request);

        validateRequestReadyForPayment(request);
        CareServiceAgreement agreement = agreementService.requireAcceptedForUpdate(request);
        if (!reservationService.revalidateBeforePayment(request))
        {
            agreementService.invalidateCurrent(requestId, "Doctor or reservation became invalid before payment");
            throw new DoctorReservationInvalidException();
        }

        ConsultationPayment latestAttempt = paymentRepository
                .findFirstByAgreementIdOrderByAttemptNumberDesc(agreement.getId()).orElse(null);
        if (latestAttempt != null && latestAttempt.getStatus() == ConsultationPaymentStatus.PENDING
                && latestAttempt.getExpiresAt().isAfter(Instant.now()))
            return toResponse(latestAttempt);
        if (latestAttempt != null && latestAttempt.getStatus() == ConsultationPaymentStatus.PAID)
            return toResponse(latestAttempt);

        int attemptNumber = latestAttempt == null ? 1 : latestAttempt.getAttemptNumber() + 1;
        ConsultationPayment payment = createPendingPayment(request, agreement, attemptNumber);
        try {
            PayOSPaymentLink paymentLink = paymentGateway.createPaymentLink(
                    payment.getOrderCode(),
                    toVndMinorUnit(payment.getAmount()),
                    "HS " + payment.getOrderCode(),
                    returnUrl,
                    cancelUrl,
                    payment.getExpiresAt()
            );
            payment.setPaymentLinkId(paymentLink.getPaymentLinkId());
            payment.setCheckoutUrl(paymentLink.getCheckoutUrl());
            payment = paymentRepository.saveAndFlush(payment);
            return toResponse(payment);
        } catch (DataIntegrityViolationException exception) {
            return paymentRepository.findFirstByAgreementIdOrderByAttemptNumberDesc(agreement.getId())
                    .map(this::toResponse)
                    .orElseThrow(() -> exception);
        } catch (AppException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new AppException(ErrorCode.PAYMENT_PROVIDER_ERROR, exception.getMessage());
        }
    }

    @Override
    @Transactional
    public ConsultationPaymentResponse getPayment(Long memberId, Long requestId) {
        ConsultationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_REQUEST_NOT_FOUND));
        validateMemberOwnsRequest(memberId, request);
        ConsultationPayment payment = paymentRepository.findFirstByRequestIdOrderByAttemptNumberDesc(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_PAYMENT_NOT_FOUND));
        return toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultationPaymentResponse> getPaymentAttempts(Long memberId, Long requestId) {
        ConsultationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_REQUEST_NOT_FOUND));
        validateMemberOwnsRequest(memberId, request);
        return paymentRepository.findByRequestIdOrderByAttemptNumberAsc(requestId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ConsultationPaymentResponse createRenewalPayment(Long memberId, Long renewalId) {
        ConsultationRenewal renewal = renewalService.requireWaitingPaymentForUpdate(memberId, renewalId);
        CareServiceAgreement agreement = agreementService.requireAcceptedForRenewal(renewal);
        ConsultationPayment latestAttempt = paymentRepository
                .findFirstByAgreementIdOrderByAttemptNumberDesc(agreement.getId()).orElse(null);
        if (latestAttempt != null && latestAttempt.getStatus() == ConsultationPaymentStatus.PENDING
                && latestAttempt.getExpiresAt().isAfter(Instant.now()))
            return toResponse(latestAttempt);
        if (latestAttempt != null && latestAttempt.getStatus() == ConsultationPaymentStatus.PAID)
            return toResponse(latestAttempt);

        int attemptNumber = latestAttempt == null ? 1 : latestAttempt.getAttemptNumber() + 1;
        ConsultationPayment payment = createPendingRenewalPayment(renewal, agreement, attemptNumber);
        try {
            PayOSPaymentLink paymentLink = paymentGateway.createPaymentLink(
                    payment.getOrderCode(), toVndMinorUnit(payment.getAmount()),
                    "HS REN " + payment.getOrderCode(), returnUrl, cancelUrl, payment.getExpiresAt());
            payment.setPaymentLinkId(paymentLink.getPaymentLinkId());
            payment.setCheckoutUrl(paymentLink.getCheckoutUrl());
            return toResponse(paymentRepository.saveAndFlush(payment));
        } catch (DataIntegrityViolationException exception) {
            return paymentRepository.findFirstByAgreementIdOrderByAttemptNumberDesc(agreement.getId())
                    .map(this::toResponse).orElseThrow(() -> exception);
        } catch (AppException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new AppException(ErrorCode.PAYMENT_PROVIDER_ERROR, exception.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultationPaymentResponse> getRenewalPaymentAttempts(Long memberId, Long renewalId) {
        ConsultationRenewal renewal = renewalService.requireOwned(memberId, renewalId);
        return paymentRepository.findByRenewalIdOrderByAttemptNumberAsc(renewal.getId()).stream()
                .map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void handlePayOSWebhook(Webhook webhook) {
        VerifiedPayOSPayment verifiedPayment;
        try {
            verifiedPayment = paymentGateway.verifyWebhook(webhook);
        } catch (Exception exception) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_WEBHOOK, exception.getMessage());
        }
        log.info("payOS webhook verified, orderCode={}", verifiedPayment.getOrderCode());

        log.info("Looking up consultation payment orderCode={}", verifiedPayment.getOrderCode());
        ConsultationPayment discoveredPayment = paymentRepository.findByOrderCode(verifiedPayment.getOrderCode())
                .orElse(null);
        if (discoveredPayment == null) {
            handleUnknownVerifiedWebhook(verifiedPayment);
            return;
        }
        if (discoveredPayment.getStatus() == ConsultationPaymentStatus.PAID
                || discoveredPayment.getStatus() == ConsultationPaymentStatus.REQUIRES_REVIEW)
            return;
        if (!validateVerifiedPayment(discoveredPayment, verifiedPayment)) {
            ConsultationPayment invalidPayment = paymentRepository
                    .findByOrderCodeForUpdate(verifiedPayment.getOrderCode())
                    .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_PAYMENT_NOT_FOUND));
            markRequiresReview(invalidPayment, Instant.now());
            return;
        }
        ConsultationRequest request = null;
        if (discoveredPayment.getPaymentPurpose() == ConsultationPaymentPurpose.INITIAL_CARE) {
            request = requestRepository.findByIdForUpdate(discoveredPayment.getRequestId())
                    .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_REQUEST_NOT_FOUND));
        } else {
            renewalService.lockSessionForPayment(discoveredPayment.getRenewalId());
        }
        ConsultationPayment payment = paymentRepository.findByOrderCodeForUpdate(verifiedPayment.getOrderCode())
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_PAYMENT_NOT_FOUND));
        if (payment.getStatus() == ConsultationPaymentStatus.PAID)
            return;
        if (payment.getStatus() == ConsultationPaymentStatus.REQUIRES_REVIEW)
            return;

        Instant now = Instant.now();
        if (payment.getPaymentPurpose() == ConsultationPaymentPurpose.RENEWAL) {
            renewalService.applyVerifiedPayment(payment, now);
            return;
        }
        activatePaidPayment(payment, request, now);
    }

    @Override
    @Transactional
    public void expireOverduePayments() {
        Instant now = Instant.now();
        paymentRepository.findByStatusInAndExpiresAtBefore(EXPIRABLE_PAYMENT_STATUSES, now)
                .forEach(payment -> reconcileOrExpire(payment, now));

        renewalService.expireOverdueRenewals(now);

        requestRepository.findByStatusInAndPaymentDeadlineBefore(
                        List.of(
                                ConsultationRequestStatus.WAITING_ACCEPTANCE,
                                ConsultationRequestStatus.WAITING_PAYMENT
                        ),
                        now
                )
                .forEach(request -> paymentRepository.findFirstByRequestIdOrderByAttemptNumberDesc(request.getId())
                        .ifPresentOrElse(
                                payment -> reconcileOrExpire(payment, now),
                                () -> expireRequest(request, now)
                        ));
    }

    private ConsultationPayment createPendingPayment(
            ConsultationRequest request,
            CareServiceAgreement agreement,
            int attemptNumber
    ) {
        ConsultationPayment payment = ConsultationPayment.builder()
                .requestId(request.getId())
                .paymentPurpose(ConsultationPaymentPurpose.INITIAL_CARE)
                .agreementId(agreement.getId())
                .attemptNumber(attemptNumber)
                .memberId(request.getMemberId())
                .provider(ConsultationPaymentProvider.PAYOS)
                .orderCode(generateOrderCode())
                .amount(agreement.getPriceAmount())
                .currency(agreement.getCurrency())
                .status(ConsultationPaymentStatus.PENDING)
                .expiresAt(request.getPaymentDeadline())
                .build();
        return paymentRepository.saveAndFlush(payment);
    }

    private ConsultationPayment createPendingRenewalPayment(
            ConsultationRenewal renewal, CareServiceAgreement agreement, int attemptNumber) {
        return paymentRepository.saveAndFlush(ConsultationPayment.builder()
                .renewalId(renewal.getId())
                .paymentPurpose(ConsultationPaymentPurpose.RENEWAL)
                .agreementId(agreement.getId())
                .attemptNumber(attemptNumber)
                .memberId(renewal.getMemberId())
                .provider(ConsultationPaymentProvider.PAYOS)
                .orderCode(generateOrderCode())
                .amount(agreement.getPriceAmount())
                .currency(agreement.getCurrency())
                .status(ConsultationPaymentStatus.PENDING)
                .expiresAt(renewal.getPaymentDeadline())
                .build());
    }

    private void reconcileOrExpire(ConsultationPayment payment, Instant now) {
        ConsultationRequest request = null;
        if (payment.getPaymentPurpose() == ConsultationPaymentPurpose.INITIAL_CARE) {
            request = requestRepository.findByIdForUpdate(payment.getRequestId())
                    .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_REQUEST_NOT_FOUND));
        } else {
            renewalService.lockSessionForPayment(payment.getRenewalId());
        }
        ConsultationPayment lockedPayment = paymentRepository.findByOrderCodeForUpdate(payment.getOrderCode())
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_PAYMENT_NOT_FOUND));
        if (!EXPIRABLE_PAYMENT_STATUSES.contains(lockedPayment.getStatus()))
            return;

        String providerStatus = safeProviderStatus(lockedPayment);
        if ("PAID".equalsIgnoreCase(providerStatus)) {
            // Polling status alone lacks the signed amount/currency/link evidence required for activation.
            markRequiresReview(lockedPayment, now);
            return;
        }

        lockedPayment.setStatus(toExpiredPaymentStatus(providerStatus));
        lockedPayment.setExpiredAt(now);
        paymentRepository.save(lockedPayment);
        if (lockedPayment.getPaymentPurpose() == ConsultationPaymentPurpose.RENEWAL) {
            renewalService.expireForPayment(lockedPayment, now);
            return;
        }
        if (request.getStatus() == ConsultationRequestStatus.WAITING_PAYMENT)
            expireRequest(request, now);
    }

    private void activatePaidPayment(ConsultationPayment payment, ConsultationRequest request, Instant now) {
        if (request.getStatus() == ConsultationRequestStatus.EXPIRED
                || request.getStatus() == ConsultationRequestStatus.CANCELLED) {
            markRequiresReview(payment, now);
            return;
        }
        if (request.getStatus() == ConsultationRequestStatus.FULFILLED
                || request.getStatus() != ConsultationRequestStatus.WAITING_PAYMENT) {
            markRequiresReview(payment, now);
            return;
        }

        CareServiceAgreement agreement;
        try {
            agreement = agreementService.requireAcceptedForUpdate(request);
        } catch (AppException exception) {
            markRequiresReview(payment, now);
            return;
        }
        if (!agreement.getId().equals(payment.getAgreementId())
                || agreement.getPriceAmount().compareTo(payment.getAmount()) != 0
                || !agreement.getCurrency().equalsIgnoreCase(payment.getCurrency())) {
            markRequiresReview(payment, now);
            return;
        }
        ConsultationPayment successfulAttempt = paymentRepository
                .findFirstByAgreementIdAndStatusOrderByAttemptNumberDesc(
                        agreement.getId(), ConsultationPaymentStatus.PAID)
                .orElse(null);
        if (successfulAttempt != null && !successfulAttempt.getId().equals(payment.getId())) {
            markRequiresReview(payment, now);
            return;
        }
        if (!reservationService.revalidateBeforeActivation(request)) {
            agreementService.invalidateCurrent(request.getId(),
                    "Doctor or reservation became invalid before activation");
            markRequiresReview(payment, now);
            return;
        }
        if (sessionRepository.findByRequestId(request.getId()).isPresent()) {
            markRequiresReview(payment, now);
            return;
        }

        ConsultationSession session = createActiveSession(request, agreement, now);
        request.setStatus(ConsultationRequestStatus.FULFILLED);
        request.setConsultationSessionId(session.getId());
        requestRepository.save(request);
        agreementService.consume(agreement);
        reservationService.release(request, DoctorReservationReleaseReason.ACTIVATED);

        payment.setStatus(ConsultationPaymentStatus.PAID);
        payment.setPaidAt(now);
        paymentRepository.save(payment);
    }

    private ConsultationSession createActiveSession(
            ConsultationRequest request,
            CareServiceAgreement agreement,
            Instant now
    ) {
        Instant endsAt = now.plus(agreement.getDurationDays(), ChronoUnit.DAYS);
        ConsultationSession session = ConsultationSession.builder()
                .memberId(request.getMemberId())
                .doctorId(request.getAssignedDoctorId())
                .sourceType(ConsultationSourceType.MEMBER_REQUEST)
                .status(ConsultationStatus.ACTIVE)
                .startedAt(now)
                .activatedAt(now)
                .endsAt(endsAt)
                .supportEndsAt(endsAt)
                .supportScheduleSnapshotJson(agreement.getSupportScheduleSnapshotJson())
                .supportTimezoneSnapshot(agreement.getSupportTimezoneSnapshot())
                .packageId(agreement.getPackageId())
                .packageVersion(agreement.getPackageVersion())
                .packagePriceSnapshot(agreement.getPriceAmount())
                .packageDurationDaysSnapshot(agreement.getDurationDays())
                .healthRecordId(request.getHealthRecordId())
                .requestId(request.getId())
                .build();
        session = sessionRepository.saveAndFlush(session);

        participantRepository.save(ConsultationParticipant.builder()
                .sessionId(session.getId())
                .userId(request.getMemberId())
                .role(ConsultationParticipantRole.MEMBER)
                .joinedAt(now)
                .active(true)
                .build());
        participantRepository.save(ConsultationParticipant.builder()
                .sessionId(session.getId())
                .userId(request.getAssignedDoctorId())
                .role(ConsultationParticipantRole.DOCTOR)
                .joinedAt(now)
                .active(true)
                .build());
        List<Long> initialRecordIds = request.getSelectedHealthRecordIds();
        if ((initialRecordIds == null || initialRecordIds.isEmpty()) && request.getHealthRecordId() != null)
            initialRecordIds = List.of(request.getHealthRecordId());
        authorizationService.authorizeInitialRecords(session,
                initialRecordIds == null ? List.of() : initialRecordIds);
        return session;
    }

    private void validateRequestReadyForPayment(ConsultationRequest request) {
        if (request.getStatus() != ConsultationRequestStatus.WAITING_PAYMENT)
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS);
        if (request.getAssignedDoctorId() == null || request.getPaymentDeadline() == null)
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS);
        if (request.getPaymentDeadline().isBefore(Instant.now()))
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS);
        if (request.getPackagePriceSnapshot() == null || request.getPackagePriceSnapshot().signum() <= 0)
            throw new AppException(ErrorCode.INVALID_PARAMETER, "Consultation package price snapshot is invalid");
        if (request.getPackageDurationDaysSnapshot() == null || request.getPackageDurationDaysSnapshot() <= 0)
            throw new AppException(ErrorCode.INVALID_PARAMETER, "Consultation package duration snapshot is invalid");
    }

    private void validateMemberOwnsRequest(Long memberId, ConsultationRequest request) {
        if (!request.getMemberId().equals(memberId))
            throw new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED);
    }

    private boolean validateVerifiedPayment(ConsultationPayment payment, VerifiedPayOSPayment verifiedPayment) {
        if (verifiedPayment.getAmount() == null || verifiedPayment.getAmount().compareTo(toVndMinorUnit(payment.getAmount())) != 0)
            return false;
        if (verifiedPayment.getCurrency() != null && !payment.getCurrency().equalsIgnoreCase(verifiedPayment.getCurrency()))
            return false;
        if (verifiedPayment.getPaymentLinkId() != null
                && payment.getPaymentLinkId() != null
                && !payment.getPaymentLinkId().equals(verifiedPayment.getPaymentLinkId()))
            return false;
        return true;
    }

    private void handleUnknownVerifiedWebhook(VerifiedPayOSPayment verifiedPayment) {
        if (!isHealthSenseOrderCode(verifiedPayment.getOrderCode())) {
            log.warn("Acknowledging signed payOS webhook for non-HealthSense orderCode={}", verifiedPayment.getOrderCode());
            return;
        }
        log.error("Signed payOS webhook references missing HealthSense payment orderCode={}", verifiedPayment.getOrderCode());
        throw new AppException(ErrorCode.CONSULTATION_PAYMENT_NOT_FOUND);
    }

    private boolean isHealthSenseOrderCode(Long orderCode) {
        return orderCode != null && orderCode >= MIN_HEALTHSENSE_ORDER_CODE;
    }

    private Long toVndMinorUnit(BigDecimal amount) {
        try {
            return amount.longValueExact();
        } catch (ArithmeticException exception) {
            throw new AppException(ErrorCode.INVALID_PARAMETER, "VND payment amount must be a whole number");
        }
    }

    private ConsultationPaymentStatus toExpiredPaymentStatus(String providerStatus) {
        if ("CANCELLED".equalsIgnoreCase(providerStatus))
            return ConsultationPaymentStatus.CANCELLED;
        if ("FAILED".equalsIgnoreCase(providerStatus))
            return ConsultationPaymentStatus.FAILED;
        return ConsultationPaymentStatus.EXPIRED;
    }

    private String safeProviderStatus(ConsultationPayment payment) {
        try {
            return paymentGateway.getPaymentStatus(payment.getOrderCode());
        } catch (Exception exception) {
            log.warn("Could not reconcile payOS order {} before expiration: {}", payment.getOrderCode(), exception.getMessage());
            return null;
        }
    }

    private void expireRequest(ConsultationRequest request, Instant now) {
        if (request.getStatus() != ConsultationRequestStatus.WAITING_PAYMENT
                && request.getStatus() != ConsultationRequestStatus.WAITING_ACCEPTANCE)
            return;
        reservationService.release(request, DoctorReservationReleaseReason.RESERVATION_EXPIRED);
        agreementService.invalidateCurrent(request.getId(), "Offer/payment window expired");
        request.setStatus(ConsultationRequestStatus.EXPIRED);
        request.setExpiredAt(now);
        requestRepository.save(request);
    }

    private Long generateOrderCode() {
        Long orderCode;
        do {
            orderCode = (System.currentTimeMillis() * 1000) + ThreadLocalRandom.current().nextLong(1000);
        } while (paymentRepository.existsByOrderCode(orderCode));
        return orderCode;
    }

    private ConsultationPaymentResponse toResponse(ConsultationPayment payment) {
        return ConsultationPaymentResponse.builder()
                .id(payment.getId())
                .requestId(payment.getRequestId())
                .renewalId(payment.getRenewalId())
                .paymentPurpose(payment.getPaymentPurpose())
                .agreementId(payment.getAgreementId())
                .attemptNumber(payment.getAttemptNumber())
                .memberId(payment.getMemberId())
                .provider(payment.getProvider())
                .orderCode(payment.getOrderCode())
                .paymentLinkId(payment.getPaymentLinkId())
                .checkoutUrl(payment.getCheckoutUrl())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .expiresAt(payment.getExpiresAt())
                .paidAt(payment.getPaidAt())
                .expiredAt(payment.getExpiredAt())
                .cancelledAt(payment.getCancelledAt())
                .providerCancellationStatus(payment.getProviderCancellationStatus())
                .providerCancellationRequestedAt(payment.getProviderCancellationRequestedAt())
                .providerCancellationCompletedAt(payment.getProviderCancellationCompletedAt())
                .providerCancellationLastAttemptAt(payment.getProviderCancellationLastAttemptAt())
                .providerCancellationError(payment.getProviderCancellationError())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    private void markRequiresReview(ConsultationPayment payment, Instant now) {
        payment.setStatus(ConsultationPaymentStatus.REQUIRES_REVIEW);
        if (payment.getPaidAt() == null)
            payment.setPaidAt(now);
        paymentRepository.save(payment);
        refundReviewCaseService.ensureReviewRequired(payment);
        if (payment.getPaymentPurpose() == ConsultationPaymentPurpose.RENEWAL)
            renewalService.markRequiresReview(payment);
    }
}
