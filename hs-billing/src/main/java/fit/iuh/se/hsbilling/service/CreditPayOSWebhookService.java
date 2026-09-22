package fit.iuh.se.hsbilling.service;

import fit.iuh.se.hsbilling.dto.VerifiedCreditPayment;
import fit.iuh.se.hsbilling.entity.CreditPaymentAttempt;
import fit.iuh.se.hsbilling.entity.CreditPurchaseOrder;
import fit.iuh.se.hsbilling.entity.enums.*;
import fit.iuh.se.hsbilling.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CreditPayOSWebhookService {
    private final CreditPaymentAttemptRepository attempts;
    private final CreditOrderRepository orders;
    private final CreditPurchaseCompletionService completion;

    @Transactional
    public boolean handle(Long orderCode, Long amount, String currency, String paymentLinkId,
            String resultCode, String reference) {
        var attempt = attempts.findByOrderCode(orderCode).orElse(null);
        if (attempt == null) return false;
        if (attempt.getProvider() != CreditPaymentProvider.PAYOS) return true;
        if (!"00".equals(resultCode)) return true;
        var order = orders.findById(attempt.getOrderId()).orElse(null);
        if (order == null || amount == null || amount != order.getAmountVnd()
                || !Objects.equals(currency, order.getCurrency())
                || (attempt.getPaymentLinkId() != null && !Objects.equals(paymentLinkId, attempt.getPaymentLinkId()))) {
            requireReview(attempt, order, "Verified PayOS webhook does not match the credit order snapshot");
            return true;
        }
        if (order.getStatus() == CreditOrderStatus.CANCELLED || order.getStatus() == CreditOrderStatus.EXPIRED
                || attempt.getStatus() == CreditPaymentStatus.CANCELLED
                || attempt.getStatus() == CreditPaymentStatus.EXPIRED) {
            requireReview(attempt, order, "Verified PayOS payment arrived after the order was closed");
            return true;
        }
        completion.completePurchase(order.getId(), attempt.getId(),
                new VerifiedCreditPayment(CreditPaymentProvider.PAYOS, reference, paymentLinkId,
                        amount, currency));
        return true;
    }

    private void requireReview(CreditPaymentAttempt attempt, CreditPurchaseOrder order, String reason) {
        if (attempt.getStatus() == CreditPaymentStatus.PAID) return;
        attempt.setStatus(CreditPaymentStatus.REQUIRES_REVIEW);
        attempt.setLastError(reason);
        attempts.saveAndFlush(attempt);
        if (order != null && order.getStatus() != CreditOrderStatus.PAID) {
            order.setStatus(CreditOrderStatus.REQUIRES_REVIEW);
            orders.saveAndFlush(order);
        }
    }
}
