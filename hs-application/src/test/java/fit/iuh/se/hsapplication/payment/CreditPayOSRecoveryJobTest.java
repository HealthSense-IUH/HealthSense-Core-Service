package fit.iuh.se.hsapplication.payment;

import fit.iuh.se.hsbilling.entity.CreditPurchaseOrder;
import fit.iuh.se.hsbilling.entity.enums.CreditOrderStatus;
import fit.iuh.se.hsbilling.payment.CreditPaymentGateway;
import fit.iuh.se.hsbilling.repository.CreditOrderRepository;
import fit.iuh.se.hsbilling.repository.CreditPaymentAttemptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditPayOSRecoveryJobTest {

    @Mock CreditPaymentAttemptRepository attempts;
    @Mock CreditOrderRepository orders;
    @Mock CreditPaymentGateway gateway;
    @Mock TransactionTemplate transactions;

    CreditPayOSRecoveryJob job;

    @BeforeEach
    void setUp() {
        job = new CreditPayOSRecoveryJob(attempts, orders, gateway, transactions);
    }

    @Test
    @SuppressWarnings("unchecked")
    void expiresOrphanPendingOrdersWithNoAttempts() {
        CreditPurchaseOrder orphan = CreditPurchaseOrder.builder()
                .id(501L)
                .status(CreditOrderStatus.PENDING_PAYMENT)
                .build();
        when(attempts.findTop100ByProviderAndStatusInAndExpiresAtBeforeOrderByExpiresAtAsc(any(), any(), any()))
                .thenReturn(List.of());
        when(orders.findOrphanPendingOrdersCreatedBefore(any(Instant.class))).thenReturn(List.of(orphan));
        when(orders.findByIdForUpdate(501L)).thenReturn(Optional.of(orphan));
        when(attempts.findFirstByOrderIdOrderByAttemptNumberDesc(501L)).thenReturn(Optional.empty());
        doAnswer(inv -> {
            Consumer<TransactionStatus> cb = inv.getArgument(0);
            cb.accept(null);
            return null;
        }).when(transactions).executeWithoutResult(any());

        job.reconcileExpired();

        assertEquals(CreditOrderStatus.EXPIRED, orphan.getStatus());
        verify(orders).saveAndFlush(orphan);
    }
}
