package fit.iuh.se.hsapplication.payment;

import fit.iuh.se.hsbilling.entity.enums.*;
import fit.iuh.se.hsbilling.payment.CreditPaymentGateway;
import fit.iuh.se.hsbilling.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreditPayOSRecoveryJob {
    private final CreditPaymentAttemptRepository attempts;
    private final CreditOrderRepository orders;
    private final CreditPaymentGateway gateway;
    private final TransactionTemplate transactions;

    @Scheduled(fixedDelayString = "${app.billing.payos.recovery-delay-ms:60000}")
    public void reconcileExpired() {
        var candidates = attempts.findTop100ByProviderAndStatusInAndExpiresAtBeforeOrderByExpiresAtAsc(
                CreditPaymentProvider.PAYOS,
                List.of(CreditPaymentStatus.CREATING, CreditPaymentStatus.PENDING), Instant.now());
        for (var attempt : candidates) reconcile(attempt.getId(), attempt.getOrderCode());
    }

    private void reconcile(Long attemptId, Long orderCode) {
        try {
            String providerStatus = gateway.getPaymentStatus(orderCode);
            if ("PAID".equalsIgnoreCase(providerStatus)) {
                close(attemptId, CreditPaymentStatus.REQUIRES_REVIEW, CreditOrderStatus.REQUIRES_REVIEW,
                        "PayOS reports PAID but no verified webhook evidence was stored");
                return;
            }
            if ("CANCELLED".equalsIgnoreCase(providerStatus)) {
                close(attemptId, CreditPaymentStatus.CANCELLED, CreditOrderStatus.CANCELLED, null);
                return;
            }
            gateway.cancelPaymentLink(orderCode, "Credit checkout expired");
            close(attemptId, CreditPaymentStatus.EXPIRED, CreditOrderStatus.EXPIRED, null);
        } catch (RuntimeException exception) {
            log.warn("Unable to reconcile expired credit payment attempt {}", attemptId, exception);
        }
    }

    private void close(Long attemptId, CreditPaymentStatus attemptStatus, CreditOrderStatus orderStatus, String error) {
        transactions.executeWithoutResult(status -> attempts.findByIdForUpdate(attemptId).ifPresent(attempt -> {
            if (attempt.getStatus() == CreditPaymentStatus.PAID) return;
            var order = orders.findByIdForUpdate(attempt.getOrderId()).orElse(null);
            if (order == null || order.getStatus() == CreditOrderStatus.PAID) return;
            attempt.setStatus(attemptStatus); attempt.setLastError(error);
            order.setStatus(orderStatus);
            attempts.saveAndFlush(attempt); orders.saveAndFlush(order);
        }));
    }
}
