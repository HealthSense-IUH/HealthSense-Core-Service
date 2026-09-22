package fit.iuh.se.hsbilling.service;

import fit.iuh.se.hsbilling.entity.*;
import fit.iuh.se.hsbilling.entity.enums.*;
import fit.iuh.se.hsbilling.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class CreditPayOSWebhookServiceTest {
    @Mock CreditPaymentAttemptRepository attempts;
    @Mock CreditOrderRepository orders;
    @Mock CreditPurchaseCompletionService completion;
    @InjectMocks CreditPayOSWebhookService service;

    @Test void verifiedMatchingWebhookUsesSingleCompletionBoundary() {
        var attempt = CreditPaymentAttempt.builder().id(20L).orderId(10L).attemptNumber(1)
                .provider(CreditPaymentProvider.PAYOS).status(CreditPaymentStatus.PENDING)
                .orderCode(123L).paymentLinkId("link-1").build();
        var order = CreditPurchaseOrder.builder().id(10L).memberId(1L).packageId(2L)
                .packageCode("C5").packageName("Five").creditQuantity(5).amountVnd(50000)
                .currency("VND").status(CreditOrderStatus.PENDING_PAYMENT)
                .idempotencyKey("key").requestFingerprint("fingerprint").build();
        when(attempts.findByOrderCode(123L)).thenReturn(Optional.of(attempt));
        when(orders.findById(10L)).thenReturn(Optional.of(order));

        assertTrue(service.handle(123L, 50000L, "VND", "link-1", "00", "bank-ref"));
        verify(completion).completePurchase(eq(10L), eq(20L), argThat(e ->
                e.provider() == CreditPaymentProvider.PAYOS && e.reference().equals("bank-ref")
                        && e.paymentLinkId().equals("link-1") && e.amountVnd() == 50000));
    }

    @Test void unknownOrderIsLeftForLegacyRouterAndMismatchRequiresReview() {
        when(attempts.findByOrderCode(999L)).thenReturn(Optional.empty());
        assertFalse(service.handle(999L, 1L, "VND", "x", "00", "ref"));

        var attempt = CreditPaymentAttempt.builder().id(20L).orderId(10L).attemptNumber(1)
                .provider(CreditPaymentProvider.PAYOS).status(CreditPaymentStatus.PENDING)
                .orderCode(123L).paymentLinkId("link-1").build();
        var order = CreditPurchaseOrder.builder().id(10L).memberId(1L).packageId(2L)
                .packageCode("C5").packageName("Five").creditQuantity(5).amountVnd(50000)
                .currency("VND").status(CreditOrderStatus.PENDING_PAYMENT)
                .idempotencyKey("key").requestFingerprint("fingerprint").build();
        when(attempts.findByOrderCode(123L)).thenReturn(Optional.of(attempt));
        when(orders.findById(10L)).thenReturn(Optional.of(order));
        assertTrue(service.handle(123L, 49000L, "VND", "link-1", "00", "bank-ref"));
        assertEquals(CreditPaymentStatus.REQUIRES_REVIEW, attempt.getStatus());
        assertEquals(CreditOrderStatus.REQUIRES_REVIEW, order.getStatus());
        verify(completion, never()).completePurchase(anyLong(), anyLong(), any());
    }
}
