package fit.iuh.se.hsbilling.dto;

import java.time.Instant;

public record CreditPaymentLink(Long orderCode, String paymentLinkId, String checkoutUrl, Instant expiresAt) {}
