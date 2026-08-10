package fit.iuh.se.hschat.service.payment.impl;

import fit.iuh.se.hschat.dto.PayOSPaymentLink;
import fit.iuh.se.hschat.dto.VerifiedPayOSPayment;
import fit.iuh.se.hschat.dto.response.ConsultationPaymentResponse;
import fit.iuh.se.hschat.entity.ConsultationPayment;
import fit.iuh.se.hschat.entity.ConsultationRequest;
import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.enums.*;
import fit.iuh.se.hschat.repository.ConsultationParticipantRepository;
import fit.iuh.se.hschat.repository.ConsultationPaymentRepository;
import fit.iuh.se.hschat.repository.ConsultationRequestRepository;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hschat.service.payment.PayOSPaymentGateway;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import vn.payos.model.webhooks.Webhook;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultationPaymentServiceImplTest {

    @Mock
    ConsultationPaymentRepository paymentRepository;
    @Mock
    ConsultationRequestRepository requestRepository;
    @Mock
    ConsultationSessionRepository sessionRepository;
    @Mock
    ConsultationParticipantRepository participantRepository;
    @Mock
    PayOSPaymentGateway paymentGateway;

    ConsultationPaymentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ConsultationPaymentServiceImpl(
                paymentRepository,
                requestRepository,
                sessionRepository,
                participantRepository,
                paymentGateway
        );
        ReflectionTestUtils.setField(service, "returnUrl", "http://localhost:5173/payment/result");
        ReflectionTestUtils.setField(service, "cancelUrl", "http://localhost:5173/payment/cancel");
    }

    @Test
    void createPayment_reusesExistingPendingPayment() {
        ConsultationRequest request = waitingPaymentRequest();
        ConsultationPayment payment = pendingPayment();

        when(requestRepository.findByIdForUpdate(request.getId())).thenReturn(Optional.of(request));
        when(paymentRepository.findByRequestIdForUpdate(request.getId())).thenReturn(Optional.of(payment));

        ConsultationPaymentResponse response = service.createPayment(request.getMemberId(), request.getId());

        assertEquals(payment.getOrderCode(), response.getOrderCode());
        assertEquals(ConsultationPaymentStatus.PENDING, response.getStatus());
        verify(paymentGateway, never()).createPaymentLink(anyLong(), anyLong(), anyString(), anyString(), anyString(), any());
    }

    @Test
    void createPayment_createsProviderLinkFromSnapshotAmount() {
        ConsultationRequest request = waitingPaymentRequest();
        when(requestRepository.findByIdForUpdate(request.getId())).thenReturn(Optional.of(request));
        when(paymentRepository.findByRequestIdForUpdate(request.getId())).thenReturn(Optional.empty());
        when(paymentRepository.existsByOrderCode(anyLong())).thenReturn(false);
        when(paymentRepository.saveAndFlush(any(ConsultationPayment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentGateway.createPaymentLink(anyLong(), eq(100000L), anyString(), anyString(), anyString(), eq(request.getPaymentDeadline())))
                .thenReturn(PayOSPaymentLink.builder()
                        .paymentLinkId("plink_123")
                        .checkoutUrl("https://pay.payos.vn/web/123")
                        .status("PENDING")
                        .build());

        ConsultationPaymentResponse response = service.createPayment(request.getMemberId(), request.getId());

        assertEquals(ConsultationPaymentStatus.PENDING, response.getStatus());
        assertEquals("plink_123", response.getPaymentLinkId());
        assertEquals("https://pay.payos.vn/web/123", response.getCheckoutUrl());
    }

    @Test
    void handlePayOSWebhook_paidActivatesRequestAndSessionOnce() {
        ConsultationPayment payment = pendingPayment();
        ConsultationRequest request = waitingPaymentRequest();
        ConsultationSession session = ConsultationSession.builder().id(900L).requestId(request.getId()).build();

        when(paymentGateway.verifyWebhook(any(Webhook.class))).thenReturn(verifiedPayment(payment));
        when(paymentRepository.findByOrderCodeForUpdate(payment.getOrderCode())).thenReturn(Optional.of(payment));
        when(requestRepository.findByIdForUpdate(payment.getRequestId())).thenReturn(Optional.of(request));
        when(sessionRepository.findByRequestId(request.getId())).thenReturn(Optional.empty());
        when(sessionRepository.saveAndFlush(any(ConsultationSession.class))).thenReturn(session);

        service.handlePayOSWebhook(new Webhook());

        assertEquals(ConsultationPaymentStatus.PAID, payment.getStatus());
        assertEquals(ConsultationRequestStatus.FULFILLED, request.getStatus());
        assertEquals(session.getId(), request.getConsultationSessionId());
        verify(participantRepository, times(2)).save(any());
    }

    @Test
    void handlePayOSWebhook_duplicatePaidWebhookDoesNotCreateAnotherSession() {
        ConsultationPayment payment = pendingPayment();
        ConsultationRequest request = waitingPaymentRequest();
        ConsultationSession session = ConsultationSession.builder().id(900L).requestId(request.getId()).build();

        when(paymentGateway.verifyWebhook(any(Webhook.class))).thenReturn(verifiedPayment(payment));
        when(paymentRepository.findByOrderCodeForUpdate(payment.getOrderCode())).thenReturn(Optional.of(payment));
        when(requestRepository.findByIdForUpdate(payment.getRequestId())).thenReturn(Optional.of(request));
        when(sessionRepository.findByRequestId(request.getId())).thenReturn(Optional.of(session));

        service.handlePayOSWebhook(new Webhook());

        assertEquals(ConsultationPaymentStatus.PAID, payment.getStatus());
        assertEquals(ConsultationRequestStatus.FULFILLED, request.getStatus());
        verify(sessionRepository, never()).saveAndFlush(any());
        verify(participantRepository, never()).save(any());
    }

    @Test
    void handlePayOSWebhook_latePaymentAfterExpiredRequiresReview() {
        ConsultationPayment payment = pendingPayment();
        ConsultationRequest request = waitingPaymentRequest();
        request.setStatus(ConsultationRequestStatus.EXPIRED);
        request.setExpiredAt(Instant.now().minusSeconds(60));

        when(paymentGateway.verifyWebhook(any(Webhook.class))).thenReturn(verifiedPayment(payment));
        when(paymentRepository.findByOrderCodeForUpdate(payment.getOrderCode())).thenReturn(Optional.of(payment));
        when(requestRepository.findByIdForUpdate(payment.getRequestId())).thenReturn(Optional.of(request));

        service.handlePayOSWebhook(new Webhook());

        assertEquals(ConsultationPaymentStatus.REQUIRES_REVIEW, payment.getStatus());
        assertEquals(ConsultationRequestStatus.EXPIRED, request.getStatus());
        verify(sessionRepository, never()).saveAndFlush(any());
    }

    @Test
    void handlePayOSWebhook_unknownNonHealthSenseOrderIsAcknowledgedForProviderValidation() {
        VerifiedPayOSPayment providerValidationPayment = VerifiedPayOSPayment.builder()
                .orderCode(123L)
                .amount(3000L)
                .currency("VND")
                .paymentLinkId("sample_payment_link")
                .build();

        when(paymentGateway.verifyWebhook(any(Webhook.class))).thenReturn(providerValidationPayment);
        when(paymentRepository.findByOrderCodeForUpdate(providerValidationPayment.getOrderCode())).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> service.handlePayOSWebhook(new Webhook()));
        verify(requestRepository, never()).findByIdForUpdate(anyLong());
        verify(sessionRepository, never()).saveAndFlush(any());
    }

    @Test
    void handlePayOSWebhook_unknownHealthSenseOrderStillFails() {
        VerifiedPayOSPayment missingHealthSensePayment = VerifiedPayOSPayment.builder()
                .orderCode(1_786_350_551_550_783L)
                .amount(2000L)
                .currency("VND")
                .paymentLinkId("plink_missing")
                .build();

        when(paymentGateway.verifyWebhook(any(Webhook.class))).thenReturn(missingHealthSensePayment);
        when(paymentRepository.findByOrderCodeForUpdate(missingHealthSensePayment.getOrderCode())).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> service.handlePayOSWebhook(new Webhook()));
        assertEquals(ErrorCode.CONSULTATION_PAYMENT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void handlePayOSWebhook_sessionCreationFailurePropagatesForTransactionalRollback() {
        ConsultationPayment payment = pendingPayment();
        ConsultationRequest request = waitingPaymentRequest();

        when(paymentGateway.verifyWebhook(any(Webhook.class))).thenReturn(verifiedPayment(payment));
        when(paymentRepository.findByOrderCodeForUpdate(payment.getOrderCode())).thenReturn(Optional.of(payment));
        when(requestRepository.findByIdForUpdate(payment.getRequestId())).thenReturn(Optional.of(request));
        when(sessionRepository.findByRequestId(request.getId())).thenReturn(Optional.empty());
        when(sessionRepository.saveAndFlush(any(ConsultationSession.class))).thenThrow(new RuntimeException("db down"));

        assertThrows(RuntimeException.class, () -> service.handlePayOSWebhook(new Webhook()));
        assertEquals(ConsultationPaymentStatus.PENDING, payment.getStatus());
        assertEquals(ConsultationRequestStatus.WAITING_PAYMENT, request.getStatus());
    }

    @Test
    void getPayment_reconcilesPaidProviderStatusAndActivatesSession() {
        ConsultationPayment payment = pendingPayment();
        ConsultationRequest request = waitingPaymentRequest();
        ConsultationSession session = ConsultationSession.builder().id(900L).requestId(request.getId()).build();

        when(requestRepository.findByIdForUpdate(request.getId())).thenReturn(Optional.of(request));
        when(paymentRepository.findByRequestIdForUpdate(request.getId())).thenReturn(Optional.of(payment));
        when(paymentGateway.getPaymentStatus(payment.getOrderCode())).thenReturn("PAID");
        when(sessionRepository.findByRequestId(request.getId())).thenReturn(Optional.empty());
        when(sessionRepository.saveAndFlush(any(ConsultationSession.class))).thenReturn(session);

        ConsultationPaymentResponse response = service.getPayment(request.getMemberId(), request.getId());

        assertEquals(ConsultationPaymentStatus.PAID, response.getStatus());
        assertEquals(ConsultationPaymentStatus.PAID, payment.getStatus());
        assertEquals(ConsultationRequestStatus.FULFILLED, request.getStatus());
        assertEquals(session.getId(), request.getConsultationSessionId());
        verify(participantRepository, times(2)).save(any());
    }

    @Test
    void getPayment_keepsPendingWhenProviderStatusIsPending() {
        ConsultationPayment payment = pendingPayment();
        ConsultationRequest request = waitingPaymentRequest();

        when(requestRepository.findByIdForUpdate(request.getId())).thenReturn(Optional.of(request));
        when(paymentRepository.findByRequestIdForUpdate(request.getId())).thenReturn(Optional.of(payment));
        when(paymentGateway.getPaymentStatus(payment.getOrderCode())).thenReturn("PENDING");

        ConsultationPaymentResponse response = service.getPayment(request.getMemberId(), request.getId());

        assertEquals(ConsultationPaymentStatus.PENDING, response.getStatus());
        assertEquals(ConsultationRequestStatus.WAITING_PAYMENT, request.getStatus());
        verify(sessionRepository, never()).saveAndFlush(any());
        verify(participantRepository, never()).save(any());
    }

    @Test
    void expireOverduePayments_reconcilesPaidProviderStatusBeforeExpiring() {
        ConsultationPayment payment = pendingPayment();
        ConsultationRequest request = waitingPaymentRequest();
        ConsultationSession session = ConsultationSession.builder().id(900L).requestId(request.getId()).build();

        when(paymentRepository.findByStatusInAndExpiresAtBefore(anyCollection(), any())).thenReturn(java.util.List.of(payment));
        when(paymentRepository.findByOrderCodeForUpdate(payment.getOrderCode())).thenReturn(Optional.of(payment));
        when(requestRepository.findByIdForUpdate(payment.getRequestId())).thenReturn(Optional.of(request));
        when(paymentGateway.getPaymentStatus(payment.getOrderCode())).thenReturn("PAID");
        when(sessionRepository.findByRequestId(request.getId())).thenReturn(Optional.empty());
        when(sessionRepository.saveAndFlush(any(ConsultationSession.class))).thenReturn(session);
        when(requestRepository.findByStatusAndPaymentDeadlineBefore(eq(ConsultationRequestStatus.WAITING_PAYMENT), any()))
                .thenReturn(java.util.List.of());

        service.expireOverduePayments();

        assertEquals(ConsultationPaymentStatus.PAID, payment.getStatus());
        assertEquals(ConsultationRequestStatus.FULFILLED, request.getStatus());
    }

    @Test
    void createPayment_providerFailureLeavesPaymentUnpaidForTransactionRollback() {
        ConsultationRequest request = waitingPaymentRequest();
        ArgumentCaptor<ConsultationPayment> captor = ArgumentCaptor.forClass(ConsultationPayment.class);
        when(requestRepository.findByIdForUpdate(request.getId())).thenReturn(Optional.of(request));
        when(paymentRepository.findByRequestIdForUpdate(request.getId())).thenReturn(Optional.empty());
        when(paymentRepository.existsByOrderCode(anyLong())).thenReturn(false);
        when(paymentRepository.saveAndFlush(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentGateway.createPaymentLink(anyLong(), anyLong(), anyString(), anyString(), anyString(), any()))
                .thenThrow(new RuntimeException("payOS down"));

        assertThrows(AppException.class, () -> service.createPayment(request.getMemberId(), request.getId()));
        assertEquals(ConsultationPaymentStatus.PENDING, captor.getValue().getStatus());
    }

    private ConsultationRequest waitingPaymentRequest() {
        return ConsultationRequest.builder()
                .id(100L)
                .memberId(10L)
                .healthRecordId(20L)
                .packageId(30L)
                .packagePriceSnapshot(new BigDecimal("100000"))
                .packageDurationDaysSnapshot(7)
                .reason("Need care")
                .status(ConsultationRequestStatus.WAITING_PAYMENT)
                .assignedDoctorId(40L)
                .paymentDeadline(Instant.now().plusSeconds(300))
                .build();
    }

    private ConsultationPayment pendingPayment() {
        return ConsultationPayment.builder()
                .id(200L)
                .requestId(100L)
                .memberId(10L)
                .provider(ConsultationPaymentProvider.PAYOS)
                .orderCode(123456L)
                .paymentLinkId("plink_123")
                .checkoutUrl("https://pay.payos.vn/web/123")
                .amount(new BigDecimal("100000"))
                .currency("VND")
                .status(ConsultationPaymentStatus.PENDING)
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
    }

    private VerifiedPayOSPayment verifiedPayment(ConsultationPayment payment) {
        return VerifiedPayOSPayment.builder()
                .orderCode(payment.getOrderCode())
                .amount(100000L)
                .currency("VND")
                .paymentLinkId(payment.getPaymentLinkId())
                .build();
    }
}
