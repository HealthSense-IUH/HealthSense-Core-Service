package fit.iuh.se.hschat.service.payment;

import fit.iuh.se.hschat.dto.PayOSPaymentLink;
import fit.iuh.se.hschat.dto.VerifiedPayOSPayment;
import fit.iuh.se.hschat.dto.ProviderRefundResult;

import java.math.BigDecimal;
import vn.payos.model.webhooks.Webhook;

import java.time.Instant;

public interface PayOSPaymentGateway {

    PayOSPaymentLink createPaymentLink(Long orderCode, Long amount, String description, String returnUrl, String cancelUrl, Instant expiresAt);

    String getPaymentStatus(Long orderCode);

    void cancelPaymentLink(Long orderCode, String reason);

    ProviderRefundResult refundPayment(Long orderCode, BigDecimal amount, String currency, String idempotencyKey, String reason);

    VerifiedPayOSPayment verifyWebhook(Webhook webhook);
}
