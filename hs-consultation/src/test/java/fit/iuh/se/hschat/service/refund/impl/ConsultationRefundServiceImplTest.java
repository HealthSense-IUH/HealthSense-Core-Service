package fit.iuh.se.hschat.service.refund.impl;

import fit.iuh.se.hschat.dto.ProviderRefundResult;
import fit.iuh.se.hschat.dto.request.*;
import fit.iuh.se.hschat.entity.*;
import fit.iuh.se.hschat.entity.enums.*;
import fit.iuh.se.hschat.repository.*;
import fit.iuh.se.hschat.service.payment.PayOSPaymentGateway;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultationRefundServiceImplTest {
    @Mock ConsultationRefundRepository refundRepository;
    @Mock ConsultationPaymentRepository paymentRepository;
    @Mock CareServiceAgreementRepository agreementRepository;
    @Mock ConsultationRequestRepository requestRepository;
    @Mock ConsultationRenewalRepository renewalRepository;
    @Mock PayOSPaymentGateway paymentGateway;
    @Mock fit.iuh.se.hsoperations.event.OperationalEventPublisher OperationalEventPublisher;
    ConsultationRefundServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ConsultationRefundServiceImpl(refundRepository, paymentRepository,
                agreementRepository, requestRepository, renewalRepository, paymentGateway, OperationalEventPublisher);
        lenient().when(refundRepository.save(any())).thenAnswer(invocation -> {
            ConsultationRefund refund = invocation.getArgument(0);
            if (refund.getId() == null) refund.setId(800L);
            return refund;
        });
        lenient().when(refundRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void coordinatorRecommendsButCannotApproveOrExecute() {
        ConsultationPayment payment = paidPayment();
        when(paymentRepository.findByIdForUpdate(200L)).thenReturn(Optional.of(payment));
        when(agreementRepository.findById(700L)).thenReturn(Optional.of(agreement()));
        when(refundRepository.findByPaymentIdForUpdate(200L)).thenReturn(Optional.empty());

        var response = service.recommend(9L, UserRole.CARE_COORDINATOR, 200L,
                recommendation(RefundRecommendation.FULL, null));

        assertEquals(ConsultationRefundStatus.RECOMMENDED, response.getStatus());
        assertEquals(payment.getAmount(), response.getRecommendedAmount());
        assertThrows(AppException.class, () -> service.decide(9L, UserRole.CARE_COORDINATOR,
                800L, decision(true, payment.getAmount())));
        assertThrows(AppException.class, () -> service.execute(9L, UserRole.CARE_COORDINATOR, 800L));
    }

    @Test
    void doctorCannotApproveRefund() {
        assertThrows(AppException.class, () -> service.decide(2L, UserRole.DOCTOR,
                800L, decision(true, new BigDecimal("40000"))));

        verify(refundRepository, never()).findByIdForUpdate(anyLong());
        verify(paymentGateway, never()).refundPayment(anyLong(), any(), anyString(), anyString(), anyString());
    }

    @Test
    void adminCanApprovePartialRefundAndSuccessfulExecutionKeepsPaymentPaid() {
        ConsultationPayment payment = paidPayment();
        ConsultationRefund refund = recommendedRefund();
        when(refundRepository.findByIdForUpdate(800L)).thenReturn(Optional.of(refund));
        when(paymentRepository.findById(200L)).thenReturn(Optional.of(payment));
        when(paymentGateway.refundPayment(eq(123L), eq(new BigDecimal("40000")), eq("VND"),
                eq("consultation-refund-800"), anyString()))
                .thenReturn(ProviderRefundResult.builder().providerRefundId("rf_1").result("SUCCEEDED").build());

        service.decide(10L, UserRole.ADMIN, 800L, decision(true, new BigDecimal("40000")));
        var response = service.execute(10L, UserRole.ADMIN, 800L);

        assertEquals(ConsultationRefundStatus.SUCCEEDED, response.getStatus());
        assertEquals("rf_1", response.getProviderRefundId());
        assertEquals(ConsultationPaymentStatus.PAID, payment.getStatus());
        verify(OperationalEventPublisher).record(argThat(command ->
                command.eventType() == fit.iuh.se.hsoperations.entity.enums.BusinessEventType.REFUND_APPROVED));
        verify(OperationalEventPublisher).record(argThat(command ->
                command.eventType() == fit.iuh.se.hsoperations.entity.enums.BusinessEventType.REFUND_SUCCEEDED));
    }

    @Test
    void providerFailureLeavesRefundFailedAndDoesNotMutatePayment() {
        ConsultationPayment payment = paidPayment();
        ConsultationRefund refund = recommendedRefund();
        refund.setStatus(ConsultationRefundStatus.APPROVED);
        refund.setApprovedAmount(payment.getAmount());
        when(refundRepository.findByIdForUpdate(800L)).thenReturn(Optional.of(refund));
        when(paymentRepository.findById(200L)).thenReturn(Optional.of(payment));
        when(paymentGateway.refundPayment(anyLong(), any(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("provider unavailable"));

        var response = service.execute(10L, UserRole.SUPER_ADMIN, 800L);

        assertEquals(ConsultationRefundStatus.FAILED, response.getStatus());
        assertEquals(ConsultationPaymentStatus.PAID, payment.getStatus());
        assertEquals(1, response.getExecutionAttempts());
        verify(OperationalEventPublisher).record(argThat(command ->
                command.eventType() == fit.iuh.se.hsoperations.entity.enums.BusinessEventType.REFUND_FAILED
                        && command.needsAction() != null));
    }

    @Test
    void noneRecommendationCanBeRejectedAndFinalDecisionIsIdempotent() {
        ConsultationRefund refund = recommendedRefund();
        refund.setRecommendation(RefundRecommendation.NONE);
        refund.setRecommendedAmount(null);
        when(refundRepository.findByIdForUpdate(800L)).thenReturn(Optional.of(refund));
        DecideRefundRequest reject = decision(false, null);

        service.decide(10L, UserRole.ADMIN, 800L, reject);
        assertEquals(ConsultationRefundStatus.REJECTED, refund.getStatus());
        assertEquals(ConsultationRefundStatus.REJECTED,
                service.decide(10L, UserRole.ADMIN, 800L, reject).getStatus());
        verify(paymentGateway, never()).refundPayment(anyLong(), any(), anyString(), anyString(), anyString());
        verify(OperationalEventPublisher).record(argThat(command ->
                command.eventType() == fit.iuh.se.hsoperations.entity.enums.BusinessEventType.REFUND_REJECTED));
    }

    private ConsultationPayment paidPayment() {
        return ConsultationPayment.builder().id(200L).requestId(100L).agreementId(700L).attemptNumber(1)
                .memberId(10L).provider(ConsultationPaymentProvider.PAYOS).orderCode(123L)
                .amount(new BigDecimal("100000")).currency("VND").status(ConsultationPaymentStatus.PAID)
                .paidAt(Instant.now()).expiresAt(Instant.now()).build();
    }

    private CareServiceAgreement agreement() {
        return CareServiceAgreement.builder().id(700L).refundPolicyReference("refund-policy-v3")
                .validUntil(Instant.now()).build();
    }

    private ConsultationRefund recommendedRefund() {
        return ConsultationRefund.builder().id(800L).paymentId(200L).agreementId(700L).memberId(10L)
                .originalPaidAmount(new BigDecimal("100000")).currency("VND")
                .refundPolicyReference("refund-policy-v3").provider(ConsultationPaymentProvider.PAYOS)
                .recommendation(RefundRecommendation.PARTIAL).recommendedAmount(new BigDecimal("40000"))
                .reviewReason("Care ended early").status(ConsultationRefundStatus.RECOMMENDED).build();
    }

    private RecommendRefundRequest recommendation(RefundRecommendation value, BigDecimal amount) {
        RecommendRefundRequest request = new RecommendRefundRequest();
        request.setRecommendation(value); request.setRecommendedAmount(amount); request.setReason("Policy review");
        return request;
    }

    private DecideRefundRequest decision(boolean approved, BigDecimal amount) {
        DecideRefundRequest request = new DecideRefundRequest();
        request.setApproved(approved); request.setApprovedAmount(amount); request.setReason("Final decision");
        return request;
    }
}
