package fit.iuh.se.hsbilling.repository;

import java.time.Instant;

public record CreditPaymentAggregate(
        long totalPaidVnd,
        long totalPurchasedCredits,
        long successfulOrderCount,
        long payingMemberCount,
        Instant firstPaidAt,
        Instant lastPaidAt) {
}
