package fit.iuh.se.hschat.service.payment.impl;

import fit.iuh.se.hschat.dto.PayOSPaymentLink;
import fit.iuh.se.hschat.dto.VerifiedPayOSPayment;
import fit.iuh.se.hschat.service.payment.PayOSPaymentGateway;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PayOSPaymentGatewayImpl implements PayOSPaymentGateway {

    @NonFinal
    @Value("${app.payment.payos.client-id:}")
    String clientId;

    @NonFinal
    @Value("${app.payment.payos.api-key:}")
    String apiKey;

    @NonFinal
    @Value("${app.payment.payos.checksum-key:}")
    String checksumKey;

    @Override
    public PayOSPaymentLink createPaymentLink(Long orderCode, Long amount, String description, String returnUrl, String cancelUrl, Instant expiresAt) {
        var response = payOS().paymentRequests().create(CreatePaymentLinkRequest.builder()
                .orderCode(orderCode)
                .amount(amount)
                .description(description)
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .expiredAt(expiresAt.getEpochSecond())
                .build());

        return PayOSPaymentLink.builder()
                .orderCode(response.getOrderCode())
                .paymentLinkId(response.getPaymentLinkId())
                .checkoutUrl(response.getCheckoutUrl())
                .status(response.getStatus() == null ? null : response.getStatus().name())
                .build();
    }

    @Override
    public String getPaymentStatus(Long orderCode) {
        var paymentLink = payOS().paymentRequests().get(orderCode);
        return paymentLink.getStatus() == null ? null : paymentLink.getStatus().name();
    }

    @Override
    public VerifiedPayOSPayment verifyWebhook(Webhook webhook) {
        WebhookData data = payOS().webhooks().verify(webhook);
        return VerifiedPayOSPayment.builder()
                .orderCode(data.getOrderCode())
                .amount(data.getAmount())
                .currency(data.getCurrency())
                .paymentLinkId(data.getPaymentLinkId())
                .build();
    }

    private PayOS payOS() {
        if (isBlank(clientId) || isBlank(apiKey) || isBlank(checksumKey))
            throw new AppException(ErrorCode.PAYMENT_PROVIDER_NOT_CONFIGURED);
        return new PayOS(clientId, apiKey, checksumKey);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
