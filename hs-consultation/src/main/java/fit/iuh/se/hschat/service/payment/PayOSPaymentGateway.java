package fit.iuh.se.hschat.service.payment;

import fit.iuh.se.hschat.dto.PayOSPaymentLink;
import fit.iuh.se.hschat.dto.VerifiedPayOSPayment;
import vn.payos.model.webhooks.Webhook;

import java.time.Instant;

public interface PayOSPaymentGateway {

    PayOSPaymentLink createPaymentLink(Long orderCode, Long amount, String description, String returnUrl, String cancelUrl, Instant expiresAt);

    String getPaymentStatus(Long orderCode);

    VerifiedPayOSPayment verifyWebhook(Webhook webhook);
}
