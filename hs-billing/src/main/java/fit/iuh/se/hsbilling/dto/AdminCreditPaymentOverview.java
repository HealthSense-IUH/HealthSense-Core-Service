package fit.iuh.se.hsbilling.dto;

import java.time.Instant;

public record AdminCreditPaymentOverview(
        long totalPaidVnd,
        long totalPurchasedCredits,
        long successfulOrderCount,
        long payingMemberCount,
        Instant firstPaidAt,
        Instant lastPaidAt) {
}
