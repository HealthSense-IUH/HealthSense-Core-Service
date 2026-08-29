package fit.iuh.se.hschat.service.payment.impl;

import fit.iuh.se.hschat.entity.ConsultationPayment;
import fit.iuh.se.hschat.entity.enums.*;
import fit.iuh.se.hschat.repository.ConsultationPaymentRepository;
import fit.iuh.se.hschat.service.payment.PayOSPaymentGateway;
import fit.iuh.se.hschat.event.PaymentProviderCancellationRequestedEvent;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentCancellationServiceImplTest {
    @Mock ConsultationPaymentRepository paymentRepository;
    @Mock PayOSPaymentGateway paymentGateway;
    @Mock ApplicationEventPublisher eventPublisher;
    PaymentCancellationServiceImpl service;
    PaymentProviderCancellationEventHandler eventHandler;

    @BeforeEach
    void setUp() {
        service = new PaymentCancellationServiceImpl(paymentRepository, paymentGateway, eventPublisher);
        eventHandler = new PaymentProviderCancellationEventHandler(paymentRepository, paymentGateway);
    }

    @Test
    void localCancellationIsDurableEvenWhenProviderTimesOut() {
        ConsultationPayment payment = pendingPayment();
        when(paymentRepository.findByRequestIdForUpdate(100L)).thenReturn(List.of(payment));
        service.prepareRequestCancellation(100L);
        assertEquals(ConsultationPaymentStatus.CANCELLED, payment.getStatus());
        assertEquals(PaymentProviderCancellationStatus.PENDING, payment.getProviderCancellationStatus());

        assertDoesNotThrow(() -> service.cancelProviderLinksAfterCommit(100L));
        assertEquals(PaymentProviderCancellationStatus.PENDING, payment.getProviderCancellationStatus());
        verify(eventPublisher).publishEvent(new PaymentProviderCancellationRequestedEvent(100L));
        verifyNoInteractions(paymentGateway);
    }

    @Test
    void providerTimeoutIsPersistedByAsynchronousHandler() {
        ConsultationPayment payment = pendingPayment();
        payment.setStatus(ConsultationPaymentStatus.CANCELLED);
        payment.setProviderCancellationStatus(PaymentProviderCancellationStatus.PENDING);
        when(paymentRepository.findByRequestIdForUpdate(100L)).thenReturn(List.of(payment));
        doThrow(new RuntimeException("timeout")).when(paymentGateway)
                .cancelPaymentLink(payment.getOrderCode(), "HealthSense care request cancelled locally");

        assertDoesNotThrow(() -> eventHandler.onCancellationRequested(
                new PaymentProviderCancellationRequestedEvent(100L)));

        assertEquals(PaymentProviderCancellationStatus.FAILED, payment.getProviderCancellationStatus());
        assertEquals("timeout", payment.getProviderCancellationError());
        verify(paymentRepository).save(payment);
    }

    @Test
    void paidPaymentIsNeverRewrittenByRequestCancellation() {
        ConsultationPayment payment = pendingPayment();
        payment.setStatus(ConsultationPaymentStatus.PAID);
        when(paymentRepository.findByRequestIdForUpdate(100L)).thenReturn(List.of(payment));

        service.prepareRequestCancellation(100L);

        assertEquals(ConsultationPaymentStatus.PAID, payment.getStatus());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void onlyFinancialAdminCanRetryProviderCancellation() {
        ConsultationPayment payment = pendingPayment();
        payment.setProviderCancellationStatus(PaymentProviderCancellationStatus.FAILED);
        when(paymentRepository.findByIdForUpdate(200L)).thenReturn(Optional.of(payment));

        assertThrows(AppException.class,
                () -> service.reconcileProviderCancellation(9L, UserRole.CARE_COORDINATOR, 200L));
        service.reconcileProviderCancellation(9L, UserRole.ADMIN, 200L);

        assertEquals(PaymentProviderCancellationStatus.SUCCEEDED, payment.getProviderCancellationStatus());
    }

    private ConsultationPayment pendingPayment() {
        return ConsultationPayment.builder().id(200L).requestId(100L).agreementId(700L)
                .attemptNumber(1).memberId(10L).provider(ConsultationPaymentProvider.PAYOS)
                .orderCode(123L).amount(new BigDecimal("100000")).currency("VND")
                .status(ConsultationPaymentStatus.PENDING).expiresAt(Instant.now().plusSeconds(300)).build();
    }
}
