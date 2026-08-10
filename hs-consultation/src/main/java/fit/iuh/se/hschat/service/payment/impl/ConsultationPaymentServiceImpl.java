package fit.iuh.se.hschat.service.payment.impl;

import fit.iuh.se.hschat.dto.PayOSPaymentLink;
import fit.iuh.se.hschat.dto.VerifiedPayOSPayment;
import fit.iuh.se.hschat.dto.response.ConsultationPaymentResponse;
import fit.iuh.se.hschat.entity.ConsultationParticipant;
import fit.iuh.se.hschat.entity.ConsultationPayment;
import fit.iuh.se.hschat.entity.ConsultationRequest;
import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.enums.*;
import fit.iuh.se.hschat.repository.ConsultationParticipantRepository;
import fit.iuh.se.hschat.repository.ConsultationPaymentRepository;
import fit.iuh.se.hschat.repository.ConsultationRequestRepository;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hschat.repository.DoctorCareProfileRepository;
import fit.iuh.se.hschat.service.payment.ConsultationPaymentService;
import fit.iuh.se.hschat.service.payment.PayOSPaymentGateway;
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

    static final String DEFAULT_CURRENCY = "VND";
    static final List<ConsultationPaymentStatus> EXPIRABLE_PAYMENT_STATUSES = List.of(
            ConsultationPaymentStatus.PENDING,
            ConsultationPaymentStatus.FAILED
    );
    static final long MIN_HEALTHSENSE_ORDER_CODE = 1_000_000_000_000_000L;

    ConsultationPaymentRepository paymentRepository;
    ConsultationRequestRepository requestRepository;
    ConsultationSessionRepository sessionRepository;
    ConsultationParticipantRepository participantRepository;
    DoctorCareProfileRepository doctorCareProfileRepository;
    PayOSPaymentGateway paymentGateway;

    @NonFinal
    @Value("${app.payment.return-url:http://localhost:5173/payment/result}")
    String returnUrl;

    @NonFinal
    @Value("${app.payment.cancel-url:http://localhost:5173/payment/cancel}")
    String cancelUrl;

    @Override
    @Transactional
    public ConsultationPaymentResponse createPayment(Long memberId, Long requestId) {
        ConsultationRequest request = requestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_REQUEST_NOT_FOUND));
        validateMemberOwnsRequest(memberId, request);

        ConsultationPayment existingPayment = paymentRepository.findByRequestIdForUpdate(requestId).orElse(null);
        if (existingPayment != null) {
            if (existingPayment.getStatus() == ConsultationPaymentStatus.PENDING)
                return toResponse(existingPayment);
            if (existingPayment.getStatus() == ConsultationPaymentStatus.PAID && request.getConsultationSessionId() != null)
                return toResponse(existingPayment);
            throw new AppException(ErrorCode.INVALID_CONSULTATION_PAYMENT_STATUS);
        }

        validateRequestReadyForPayment(request);
        ConsultationPayment payment = createPendingPayment(request);
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
            return paymentRepository.findByRequestId(requestId)
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
        ConsultationRequest request = requestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_REQUEST_NOT_FOUND));
        validateMemberOwnsRequest(memberId, request);
        ConsultationPayment payment = paymentRepository.findByRequestIdForUpdate(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_PAYMENT_NOT_FOUND));
        reconcilePaidProviderStatus(payment, request, Instant.now());
        return toResponse(payment);
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
        ConsultationPayment payment = paymentRepository.findByOrderCodeForUpdate(verifiedPayment.getOrderCode())
                .orElse(null);
        if (payment == null) {
            handleUnknownVerifiedWebhook(verifiedPayment);
            return;
        }
        validateVerifiedPayment(payment, verifiedPayment);

        if (payment.getStatus() == ConsultationPaymentStatus.PAID)
            return;
        if (payment.getStatus() == ConsultationPaymentStatus.REQUIRES_REVIEW)
            return;

        ConsultationRequest request = requestRepository.findByIdForUpdate(payment.getRequestId())
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_REQUEST_NOT_FOUND));
        activatePaidPayment(payment, request, Instant.now());
    }

    @Override
    @Transactional
    public void expireOverduePayments() {
        Instant now = Instant.now();
        paymentRepository.findByStatusInAndExpiresAtBefore(EXPIRABLE_PAYMENT_STATUSES, now)
                .forEach(payment -> reconcileOrExpire(payment, now));

        requestRepository.findByStatusAndPaymentDeadlineBefore(ConsultationRequestStatus.WAITING_PAYMENT, now)
                .forEach(request -> paymentRepository.findByRequestIdForUpdate(request.getId())
                        .ifPresentOrElse(
                                payment -> reconcileOrExpire(payment, now),
                                () -> expireRequest(request, now)
                        ));
    }

    private ConsultationPayment createPendingPayment(ConsultationRequest request) {
        ConsultationPayment payment = ConsultationPayment.builder()
                .requestId(request.getId())
                .memberId(request.getMemberId())
                .provider(ConsultationPaymentProvider.PAYOS)
                .orderCode(generateOrderCode())
                .amount(request.getPackagePriceSnapshot())
                .currency(DEFAULT_CURRENCY)
                .status(ConsultationPaymentStatus.PENDING)
                .expiresAt(request.getPaymentDeadline())
                .build();
        return paymentRepository.saveAndFlush(payment);
    }

    private void reconcileOrExpire(ConsultationPayment payment, Instant now) {
        ConsultationPayment lockedPayment = paymentRepository.findByOrderCodeForUpdate(payment.getOrderCode())
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_PAYMENT_NOT_FOUND));
        ConsultationRequest request = requestRepository.findByIdForUpdate(lockedPayment.getRequestId())
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_REQUEST_NOT_FOUND));

        String providerStatus = safeProviderStatus(lockedPayment);
        if ("PAID".equalsIgnoreCase(providerStatus)) {
            activatePaidPayment(lockedPayment, request, now);
            return;
        }

        lockedPayment.setStatus(toExpiredPaymentStatus(providerStatus));
        lockedPayment.setExpiredAt(now);
        paymentRepository.save(lockedPayment);
        if (request.getStatus() == ConsultationRequestStatus.WAITING_PAYMENT)
            expireRequest(request, now);
    }

    private void reconcilePaidProviderStatus(ConsultationPayment payment, ConsultationRequest request, Instant now) {
        if (payment.getStatus() != ConsultationPaymentStatus.PENDING)
            return;

        String providerStatus = safeProviderStatus(payment);
        if ("PAID".equalsIgnoreCase(providerStatus))
            activatePaidPayment(payment, request, now);
    }

    private void activatePaidPayment(ConsultationPayment payment, ConsultationRequest request, Instant now) {
        if (request.getStatus() == ConsultationRequestStatus.EXPIRED) {
            payment.setStatus(ConsultationPaymentStatus.REQUIRES_REVIEW);
            payment.setPaidAt(now);
            paymentRepository.save(payment);
            return;
        }

        if (request.getStatus() == ConsultationRequestStatus.FULFILLED && request.getConsultationSessionId() != null) {
            payment.setStatus(ConsultationPaymentStatus.PAID);
            if (payment.getPaidAt() == null)
                payment.setPaidAt(now);
            paymentRepository.save(payment);
            return;
        }

        if (request.getStatus() != ConsultationRequestStatus.WAITING_PAYMENT)
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS);

        ConsultationSession session = sessionRepository.findByRequestId(request.getId())
                .orElseGet(() -> createActiveSession(request, now));

        request.setStatus(ConsultationRequestStatus.FULFILLED);
        request.setConsultationSessionId(session.getId());
        requestRepository.save(request);

        payment.setStatus(ConsultationPaymentStatus.PAID);
        payment.setPaidAt(now);
        paymentRepository.save(payment);
    }

    private ConsultationSession createActiveSession(ConsultationRequest request, Instant now) {
        Instant endsAt = now.plus(request.getPackageDurationDaysSnapshot(), ChronoUnit.DAYS);
        var doctorProfile = doctorCareProfileRepository.findByDoctorId(request.getAssignedDoctorId())
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_CARE_PROFILE_NOT_FOUND));
        ConsultationSession session = ConsultationSession.builder()
                .memberId(request.getMemberId())
                .doctorId(request.getAssignedDoctorId())
                .sourceType(ConsultationSourceType.MEMBER_REQUEST)
                .status(ConsultationStatus.ACTIVE)
                .startedAt(now)
                .endsAt(endsAt)
                .supportEndsAt(endsAt)
                .supportScheduleSnapshotJson(doctorProfile.getAvailabilityJson())
                .supportTimezoneSnapshot(doctorProfile.getTimezone())
                .packageId(request.getPackageId())
                .packagePriceSnapshot(request.getPackagePriceSnapshot())
                .packageDurationDaysSnapshot(request.getPackageDurationDaysSnapshot())
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

    private void validateVerifiedPayment(ConsultationPayment payment, VerifiedPayOSPayment verifiedPayment) {
        if (verifiedPayment.getAmount() == null || verifiedPayment.getAmount().compareTo(toVndMinorUnit(payment.getAmount())) != 0)
            throw new AppException(ErrorCode.INVALID_PAYMENT_WEBHOOK, "Webhook amount does not match payment amount");
        if (verifiedPayment.getCurrency() != null && !payment.getCurrency().equalsIgnoreCase(verifiedPayment.getCurrency()))
            throw new AppException(ErrorCode.INVALID_PAYMENT_WEBHOOK, "Webhook currency does not match payment currency");
        if (verifiedPayment.getPaymentLinkId() != null
                && payment.getPaymentLinkId() != null
                && !payment.getPaymentLinkId().equals(verifiedPayment.getPaymentLinkId()))
            throw new AppException(ErrorCode.INVALID_PAYMENT_WEBHOOK, "Webhook payment link does not match payment");
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
        if (request.getStatus() != ConsultationRequestStatus.WAITING_PAYMENT)
            return;
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
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
