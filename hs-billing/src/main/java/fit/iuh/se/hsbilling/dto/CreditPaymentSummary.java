package fit.iuh.se.hsbilling.dto;

import fit.iuh.se.hsbilling.entity.enums.*;
import java.time.Instant;

public record CreditPaymentSummary(String attemptId, CreditPaymentProvider provider, CreditPaymentStatus status,
        String orderCode, String paymentLinkId, String checkoutUrl, Instant expiresAt) {
    public CreditPaymentSummary(String attemptId, CreditPaymentProvider provider, CreditPaymentStatus status,
            String checkoutUrl, String ignoredQrCode, Instant expiresAt) {
        this(attemptId, provider, status, null, null, checkoutUrl, expiresAt);
    }
}
