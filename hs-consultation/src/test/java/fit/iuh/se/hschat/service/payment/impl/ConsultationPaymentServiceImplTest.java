package fit.iuh.se.hschat.service.payment.impl;

import fit.iuh.se.hschat.dto.PayOSPaymentLink;
import fit.iuh.se.hschat.dto.VerifiedPayOSPayment;
import fit.iuh.se.hschat.dto.response.ConsultationPaymentResponse;
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
import fit.iuh.se.hschat.service.agreement.CareServiceAgreementService;
import fit.iuh.se.hschat.service.authorization.EpisodeHealthRecordAuthorizationService;
import fit.iuh.se.hschat.service.payment.PayOSPaymentGateway;
import fit.iuh.se.hschat.service.reservation.DoctorReservationService;
import fit.iuh.se.hschat.service.reservation.DoctorReservationInvalidException;
import fit.iuh.se.hschat.service.renewal.ConsultationRenewalService;
import fit.iuh.se.hschat.service.refund.RefundReviewCaseService;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

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
    @Mock
    DoctorReservationService reservationService;
    @Mock
    CareServiceAgreementService agreementService;
    @Mock
    EpisodeHealthRecordAuthorizationService authorizationService;
    @Mock
    ConsultationRenewalService renewalService;
    @Mock
    RefundReviewCaseService refundReviewCaseService;

    ConsultationPaymentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ConsultationPaymentServiceImpl(
                paymentRepository,
                requestRepository,
                sessionRepository,
                participantRepository,
                paymentGateway,
                reservationService,
                agreementService,
                authorizationService,
                renewalService,
                refundReviewCaseService
        );
        lenient().when(reservationService.revalidateBeforePayment(any())).thenReturn(true);
        lenient().when(reservationService.revalidateBeforeActivation(any())).thenReturn(true);
        lenient().when(agreementService.requireAcceptedForUpdate(any()))
                .thenAnswer(invocation -> acceptedAgreement(invocation.getArgument(0)));
        lenient().when(paymentRepository.findByOrderCode(anyLong()))
                .thenAnswer(invocation -> paymentRepository.findByOrderCodeForUpdate(invocation.getArgument(0)));
        ReflectionTestUtils.setField(service, "returnUrl", "http://localhost:5173/payment/result");
        ReflectionTestUtils.setField(service, "cancelUrl", "http://localhost:5173/payment/cancel");
    }

    @Test
    void createPayment_reusesExistingPendingPayment() {
        ConsultationRequest request = waitingPaymentRequest();
        ConsultationPayment payment = pendingPayment();

        when(requestRepository.findByIdForUpdate(request.getId())).thenReturn(Optional.of(request));
        when(paymentRepository.findFirstByAgreementIdOrderByAttemptNumberDesc(700L)).thenReturn(Optional.of(payment));

        ConsultationPaymentResponse response = service.createPayment(request.getMemberId(), request.getId());

        assertEquals(payment.getOrderCode(), response.getOrderCode());
        assertEquals(ConsultationPaymentStatus.PENDING, response.getStatus());
        verify(paymentGateway, never()).createPaymentLink(anyLong(), anyLong(), anyString(), anyString(), anyString(), any());
    }

    @Test
    void createPayment_createsProviderLinkFromSnapshotAmount() {
        ConsultationRequest request = waitingPaymentRequest();
        when(requestRepository.findByIdForUpdate(request.getId())).thenReturn(Optional.of(request));
        when(paymentRepository.findFirstByAgreementIdOrderByAttemptNumberDesc(700L)).thenReturn(Optional.empty());
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
    void failedAttemptCanBeFollowedByAnotherAttempt() {
        ConsultationRequest request = waitingPaymentRequest();
        ConsultationPayment failed = pendingPayment();
        failed.setStatus(ConsultationPaymentStatus.FAILED);
        when(requestRepository.findByIdForUpdate(request.getId())).thenReturn(Optional.of(request));
        when(paymentRepository.findFirstByAgreementIdOrderByAttemptNumberDesc(700L)).thenReturn(Optional.of(failed));
        when(paymentRepository.existsByOrderCode(anyLong())).thenReturn(false);
        when(paymentRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentGateway.createPaymentLink(anyLong(), anyLong(), anyString(), anyString(), anyString(), any()))
                .thenReturn(PayOSPaymentLink.builder().paymentLinkId("retry_link").checkoutUrl("https://retry").build());

        ConsultationPaymentResponse response = service.createPayment(request.getMemberId(), request.getId());

        assertEquals(2, response.getAttemptNumber());
        assertEquals(700L, response.getAgreementId());
        assertEquals(ConsultationPaymentStatus.PENDING, response.getStatus());
    }

    @Test
    void createPaymentRejectsWaitingAcceptanceWithoutAcceptedAgreement() {
        ConsultationRequest request = waitingPaymentRequest();
        request.setStatus(ConsultationRequestStatus.WAITING_ACCEPTANCE);
        when(requestRepository.findByIdForUpdate(request.getId())).thenReturn(Optional.of(request));

        assertThrows(AppException.class,
                () -> service.createPayment(request.getMemberId(), request.getId()));

        verify(agreementService, never()).requireAcceptedForUpdate(any());
        verify(paymentRepository, never()).saveAndFlush(any());
    }

    @Test
    void createPaymentAtT2ReleasesInvalidReservationAndDoesNotCallProvider() {
        ConsultationRequest request = waitingPaymentRequest();
        when(requestRepository.findByIdForUpdate(request.getId())).thenReturn(Optional.of(request));
        when(reservationService.revalidateBeforePayment(request)).thenReturn(false);

        assertThrows(DoctorReservationInvalidException.class,
                () -> service.createPayment(request.getMemberId(), request.getId()));
        verify(paymentRepository, never()).saveAndFlush(any());
        verify(paymentGateway, never()).createPaymentLink(anyLong(), anyLong(), anyString(), anyString(), anyString(), any());
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
        verify(authorizationService).authorizeInitialRecords(session, java.util.List.of(20L, 21L));
        verify(sessionRepository).saveAndFlush(argThat(created ->
                "{\"weekly\":[{\"dayOfWeek\":\"MONDAY\",\"start\":\"07:00\",\"end\":\"11:00\"}]}".equals(created.getSupportScheduleSnapshotJson())
                        && "Asia/Ho_Chi_Minh".equals(created.getSupportTimezoneSnapshot())
                        && Integer.valueOf(3).equals(created.getPackageVersion())
        ));
    }

    @Test
    void expiredReservationAtT3RequiresReviewWithoutActivatingSession() {
        ConsultationPayment payment = pendingPayment();
        ConsultationRequest request = waitingPaymentRequest();
        when(paymentGateway.verifyWebhook(any(Webhook.class))).thenReturn(verifiedPayment(payment));
        when(paymentRepository.findByOrderCodeForUpdate(payment.getOrderCode())).thenReturn(Optional.of(payment));
        when(requestRepository.findByIdForUpdate(payment.getRequestId())).thenReturn(Optional.of(request));
        when(reservationService.revalidateBeforeActivation(request)).thenReturn(false);

        service.handlePayOSWebhook(new Webhook());

        assertEquals(ConsultationPaymentStatus.REQUIRES_REVIEW, payment.getStatus());
        verify(sessionRepository, never()).saveAndFlush(any());
        verify(participantRepository, never()).save(any());
    }

    @Test
    void doctorDisabledBetweenT2AndT3RequiresReviewWithoutActivatingSession() {
        ConsultationPayment payment = pendingPayment();
        ConsultationRequest request = waitingPaymentRequest();
        when(paymentGateway.verifyWebhook(any(Webhook.class))).thenReturn(verifiedPayment(payment));
        when(paymentRepository.findByOrderCodeForUpdate(payment.getOrderCode())).thenReturn(Optional.of(payment));
        when(requestRepository.findByIdForUpdate(payment.getRequestId())).thenReturn(Optional.of(request));
        when(reservationService.revalidateBeforeActivation(request)).thenReturn(false);

        service.handlePayOSWebhook(new Webhook());

        assertEquals(ConsultationPaymentStatus.REQUIRES_REVIEW, payment.getStatus());
        verify(sessionRepository, never()).saveAndFlush(any());
    }

    @Test
    void cancelAndPaidWebhookRaceCannotLeaveCancelledRequestWithActiveSession() throws Exception {
        ConsultationPayment payment = pendingPayment();
        ConsultationRequest request = waitingPaymentRequest();
        ReentrantLock requestTransactionLock = new ReentrantLock();
        AtomicBoolean sessionCreated = new AtomicBoolean();
        CountDownLatch start = new CountDownLatch(1);

        when(paymentGateway.verifyWebhook(any(Webhook.class))).thenReturn(verifiedPayment(payment));
        when(paymentRepository.findByOrderCodeForUpdate(payment.getOrderCode())).thenReturn(Optional.of(payment));
        when(requestRepository.findByIdForUpdate(payment.getRequestId())).thenAnswer(invocation -> {
            requestTransactionLock.lock();
            return Optional.of(request);
        });
        lenient().when(sessionRepository.findByRequestId(request.getId())).thenReturn(Optional.empty());
        lenient().when(sessionRepository.saveAndFlush(any())).thenAnswer(invocation -> {
            ConsultationSession session = invocation.getArgument(0);
            session.setId(900L);
            sessionCreated.set(true);
            return session;
        });

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> webhook = executor.submit(() -> {
                await(start);
                try {
                    service.handlePayOSWebhook(new Webhook());
                } finally {
                    if (requestTransactionLock.isHeldByCurrentThread())
                        requestTransactionLock.unlock();
                }
            });
            Future<?> cancellation = executor.submit(() -> {
                await(start);
                requestTransactionLock.lock();
                try {
                    if (request.getStatus() == ConsultationRequestStatus.WAITING_PAYMENT)
                        request.setStatus(ConsultationRequestStatus.CANCELLED);
                } finally {
                    requestTransactionLock.unlock();
                }
            });

            start.countDown();
            webhook.get(5, TimeUnit.SECONDS);
            cancellation.get(5, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }

        assertFalse(request.getStatus() == ConsultationRequestStatus.CANCELLED && sessionCreated.get());
        if (request.getStatus() == ConsultationRequestStatus.CANCELLED)
            assertEquals(ConsultationPaymentStatus.REQUIRES_REVIEW, payment.getStatus());
        else
            assertEquals(ConsultationRequestStatus.FULFILLED, request.getStatus());
    }

    @Test
    void handlePayOSWebhook_duplicatePaidWebhookDoesNotCreateAnotherSession() {
        ConsultationPayment payment = pendingPayment();
        payment.setStatus(ConsultationPaymentStatus.PAID);
        ConsultationRequest request = waitingPaymentRequest();
        request.setStatus(ConsultationRequestStatus.FULFILLED);
        request.setConsultationSessionId(900L);

        when(paymentGateway.verifyWebhook(any(Webhook.class))).thenReturn(verifiedPayment(payment));
        when(paymentRepository.findByOrderCodeForUpdate(payment.getOrderCode())).thenReturn(Optional.of(payment));

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
        verify(refundReviewCaseService).ensureReviewRequired(payment);
    }

    @Test
    void handlePayOSWebhookLatePaymentAfterCancelledRequiresReview() {
        ConsultationPayment payment = pendingPayment();
        ConsultationRequest request = waitingPaymentRequest();
        request.setStatus(ConsultationRequestStatus.CANCELLED);
        when(paymentGateway.verifyWebhook(any(Webhook.class))).thenReturn(verifiedPayment(payment));
        when(paymentRepository.findByOrderCodeForUpdate(payment.getOrderCode())).thenReturn(Optional.of(payment));
        when(requestRepository.findByIdForUpdate(payment.getRequestId())).thenReturn(Optional.of(request));

        service.handlePayOSWebhook(new Webhook());

        assertEquals(ConsultationPaymentStatus.REQUIRES_REVIEW, payment.getStatus());
        assertEquals(ConsultationRequestStatus.CANCELLED, request.getStatus());
        verify(sessionRepository, never()).saveAndFlush(any());
        verify(refundReviewCaseService).ensureReviewRequired(payment);
    }

    @Test
    void wrongVerifiedAmountIsPersistedForReviewWithoutActivation() {
        ConsultationPayment payment = pendingPayment();
        VerifiedPayOSPayment wrongAmount = verifiedPayment(payment);
        wrongAmount.setAmount(999L);
        when(paymentGateway.verifyWebhook(any(Webhook.class))).thenReturn(wrongAmount);
        when(paymentRepository.findByOrderCodeForUpdate(payment.getOrderCode())).thenReturn(Optional.of(payment));

        service.handlePayOSWebhook(new Webhook());

        assertEquals(ConsultationPaymentStatus.REQUIRES_REVIEW, payment.getStatus());
        verify(requestRepository, never()).findByIdForUpdate(anyLong());
        verify(sessionRepository, never()).saveAndFlush(any());
    }

    @Test
    void secondPaidAttemptCannotConsumeAgreementOrCreateAnotherSession() {
        ConsultationPayment secondAttempt = pendingPayment();
        secondAttempt.setId(201L);
        secondAttempt.setAttemptNumber(2);
        ConsultationPayment firstPaid = pendingPayment();
        firstPaid.setStatus(ConsultationPaymentStatus.PAID);
        ConsultationRequest request = waitingPaymentRequest();
        when(paymentGateway.verifyWebhook(any(Webhook.class))).thenReturn(verifiedPayment(secondAttempt));
        when(paymentRepository.findByOrderCodeForUpdate(secondAttempt.getOrderCode())).thenReturn(Optional.of(secondAttempt));
        when(requestRepository.findByIdForUpdate(secondAttempt.getRequestId())).thenReturn(Optional.of(request));
        when(paymentRepository.findFirstByAgreementIdAndStatusOrderByAttemptNumberDesc(
                700L, ConsultationPaymentStatus.PAID)).thenReturn(Optional.of(firstPaid));

        service.handlePayOSWebhook(new Webhook());

        assertEquals(ConsultationPaymentStatus.REQUIRES_REVIEW, secondAttempt.getStatus());
        verify(agreementService, never()).consume(any());
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
    void getPaymentDoesNotActivateFromUnverifiedProviderPolling() {
        ConsultationPayment payment = pendingPayment();
        ConsultationRequest request = waitingPaymentRequest();

        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(paymentRepository.findFirstByRequestIdOrderByAttemptNumberDesc(request.getId())).thenReturn(Optional.of(payment));

        ConsultationPaymentResponse response = service.getPayment(request.getMemberId(), request.getId());

        assertEquals(ConsultationPaymentStatus.PENDING, response.getStatus());
        assertEquals(ConsultationRequestStatus.WAITING_PAYMENT, request.getStatus());
        verify(paymentGateway, never()).getPaymentStatus(anyLong());
        verify(sessionRepository, never()).saveAndFlush(any());
    }

    @Test
    void getPayment_keepsPendingWhenProviderStatusIsPending() {
        ConsultationPayment payment = pendingPayment();
        ConsultationRequest request = waitingPaymentRequest();

        when(requestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        when(paymentRepository.findFirstByRequestIdOrderByAttemptNumberDesc(request.getId())).thenReturn(Optional.of(payment));

        ConsultationPaymentResponse response = service.getPayment(request.getMemberId(), request.getId());

        assertEquals(ConsultationPaymentStatus.PENDING, response.getStatus());
        assertEquals(ConsultationRequestStatus.WAITING_PAYMENT, request.getStatus());
        verify(sessionRepository, never()).saveAndFlush(any());
        verify(participantRepository, never()).save(any());
    }

    @Test
    void expireOverduePaymentsPaidPollingRequiresReviewWithoutActivation() {
        ConsultationPayment payment = pendingPayment();
        ConsultationRequest request = waitingPaymentRequest();

        when(paymentRepository.findByStatusInAndExpiresAtBefore(anyCollection(), any())).thenReturn(java.util.List.of(payment));
        when(requestRepository.findByIdForUpdate(request.getId())).thenReturn(Optional.of(request));
        when(paymentRepository.findByOrderCodeForUpdate(payment.getOrderCode())).thenReturn(Optional.of(payment));
        when(paymentGateway.getPaymentStatus(payment.getOrderCode())).thenReturn("PAID");
        when(requestRepository.findByStatusInAndPaymentDeadlineBefore(anyCollection(), any()))
                .thenReturn(java.util.List.of());

        service.expireOverduePayments();

        assertEquals(ConsultationPaymentStatus.REQUIRES_REVIEW, payment.getStatus());
        assertEquals(ConsultationRequestStatus.WAITING_PAYMENT, request.getStatus());
        verify(sessionRepository, never()).saveAndFlush(any());
    }

    @Test
    void createPayment_providerFailureLeavesPaymentUnpaidForTransactionRollback() {
        ConsultationRequest request = waitingPaymentRequest();
        ArgumentCaptor<ConsultationPayment> captor = ArgumentCaptor.forClass(ConsultationPayment.class);
        when(requestRepository.findByIdForUpdate(request.getId())).thenReturn(Optional.of(request));
        when(paymentRepository.findFirstByAgreementIdOrderByAttemptNumberDesc(700L)).thenReturn(Optional.empty());
        when(paymentRepository.existsByOrderCode(anyLong())).thenReturn(false);
        when(paymentRepository.saveAndFlush(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentGateway.createPaymentLink(anyLong(), anyLong(), anyString(), anyString(), anyString(), any()))
                .thenThrow(new RuntimeException("payOS down"));

        assertThrows(AppException.class, () -> service.createPayment(request.getMemberId(), request.getId()));
        assertEquals(ConsultationPaymentStatus.PENDING, captor.getValue().getStatus());
    }

    @Test
    void renewalPaymentRequiresAcceptedAgreementAndSupportsAnotherAttemptAfterFailure() {
        ConsultationRenewal renewal = waitingPaymentRenewal();
        CareServiceAgreement agreement = acceptedRenewalAgreement();
        ConsultationPayment failed = renewalPayment();
        failed.setStatus(ConsultationPaymentStatus.FAILED);
        when(renewalService.requireWaitingPaymentForUpdate(10L, 300L)).thenReturn(renewal);
        when(agreementService.requireAcceptedForRenewal(renewal)).thenReturn(agreement);
        when(paymentRepository.findFirstByAgreementIdOrderByAttemptNumberDesc(701L))
                .thenReturn(Optional.of(failed));
        when(paymentRepository.existsByOrderCode(anyLong())).thenReturn(false);
        when(paymentRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentGateway.createPaymentLink(anyLong(), anyLong(), anyString(), anyString(), anyString(), any()))
                .thenReturn(PayOSPaymentLink.builder().orderCode(123458L)
                        .paymentLinkId("plink_renewal_2").checkoutUrl("https://pay/renewal/2")
                        .status("PENDING").build());

        ConsultationPaymentResponse response = service.createRenewalPayment(10L, 300L);

        assertEquals(ConsultationPaymentPurpose.RENEWAL, response.getPaymentPurpose());
        assertEquals(300L, response.getRenewalId());
        assertEquals(2, response.getAttemptNumber());
        assertEquals(new BigDecimal("150000"), response.getAmount());
    }

    @Test
    void verifiedRenewalWebhookDelegatesToAtomicExtensionAndDuplicatePaidWebhookIsIgnored() {
        ConsultationPayment payment = renewalPayment();
        when(paymentGateway.verifyWebhook(any(Webhook.class))).thenReturn(verifiedPayment(payment));
        when(paymentRepository.findByOrderCodeForUpdate(payment.getOrderCode())).thenReturn(Optional.of(payment));

        service.handlePayOSWebhook(new Webhook());
        verify(renewalService).applyVerifiedPayment(eq(payment), any());

        payment.setStatus(ConsultationPaymentStatus.PAID);
        service.handlePayOSWebhook(new Webhook());
        verify(renewalService, times(1)).applyVerifiedPayment(eq(payment), any());
        verify(sessionRepository, never()).saveAndFlush(any());
    }

    @Test
    void invalidRenewalPaymentEvidenceRequiresReviewWithoutApplyingExtension() {
        ConsultationPayment payment = renewalPayment();
        VerifiedPayOSPayment wrongAmount = verifiedPayment(payment);
        wrongAmount.setAmount(1L);
        when(paymentGateway.verifyWebhook(any(Webhook.class))).thenReturn(wrongAmount);
        when(paymentRepository.findByOrderCodeForUpdate(payment.getOrderCode())).thenReturn(Optional.of(payment));

        service.handlePayOSWebhook(new Webhook());

        assertEquals(ConsultationPaymentStatus.REQUIRES_REVIEW, payment.getStatus());
        verify(renewalService).markRequiresReview(payment);
        verify(renewalService, never()).applyVerifiedPayment(any(), any());
    }

    private ConsultationRequest waitingPaymentRequest() {
        return ConsultationRequest.builder()
                .id(100L)
                .memberId(10L)
                .healthRecordId(20L)
                .selectedHealthRecordIds(java.util.List.of(20L, 21L))
                .packageId(30L)
                .packageVersion(3)
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
                .agreementId(700L)
                .attemptNumber(1)
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

    private CareServiceAgreement acceptedAgreement(ConsultationRequest request) {
        return CareServiceAgreement.builder()
                .id(700L)
                .requestId(request.getId())
                .memberId(request.getMemberId())
                .doctorId(request.getAssignedDoctorId())
                .packageId(request.getPackageId())
                .packageVersion(request.getPackageVersion())
                .priceAmount(request.getPackagePriceSnapshot())
                .currency("VND")
                .durationDays(request.getPackageDurationDaysSnapshot())
                .supportScheduleSnapshotJson("{\"weekly\":[{\"dayOfWeek\":\"MONDAY\",\"start\":\"07:00\",\"end\":\"11:00\"}]}")
                .supportTimezoneSnapshot("Asia/Ho_Chi_Minh")
                .status(CareServiceAgreementStatus.ACCEPTED)
                .validUntil(request.getPaymentDeadline())
                .build();
    }

    private VerifiedPayOSPayment verifiedPayment(ConsultationPayment payment) {
        return VerifiedPayOSPayment.builder()
                .orderCode(payment.getOrderCode())
                .amount(payment.getAmount().longValueExact())
                .currency("VND")
                .paymentLinkId(payment.getPaymentLinkId())
                .build();
    }

    private ConsultationRenewal waitingPaymentRenewal() {
        return ConsultationRenewal.builder().id(300L).sessionId(900L).memberId(10L).doctorId(40L)
                .packageFamilyId(50L).packageId(31L).packageVersion(4).durationDays(30)
                .priceAmount(new BigDecimal("150000")).currency("VND")
                .status(ConsultationRenewalStatus.WAITING_PAYMENT)
                .requestedAt(Instant.now()).paymentDeadline(Instant.now().plusSeconds(300)).build();
    }

    private CareServiceAgreement acceptedRenewalAgreement() {
        return CareServiceAgreement.builder().id(701L).renewalId(300L)
                .agreementType(CareServiceAgreementType.RENEWAL).memberId(10L).doctorId(40L)
                .priceAmount(new BigDecimal("150000")).currency("VND")
                .status(CareServiceAgreementStatus.ACCEPTED)
                .validUntil(Instant.now().plusSeconds(300)).build();
    }

    private ConsultationPayment renewalPayment() {
        return ConsultationPayment.builder().id(201L).renewalId(300L)
                .paymentPurpose(ConsultationPaymentPurpose.RENEWAL).agreementId(701L).attemptNumber(1)
                .memberId(10L).provider(ConsultationPaymentProvider.PAYOS).orderCode(123457L)
                .paymentLinkId("plink_renewal").amount(new BigDecimal("150000")).currency("VND")
                .status(ConsultationPaymentStatus.PENDING).expiresAt(Instant.now().plusSeconds(300)).build();
    }

    private void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }

}
