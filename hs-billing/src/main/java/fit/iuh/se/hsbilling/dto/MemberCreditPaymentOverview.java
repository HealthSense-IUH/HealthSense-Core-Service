package fit.iuh.se.hsbilling.dto;

import java.time.Instant;

public record MemberCreditPaymentOverview(
        long totalPaidVnd,
        long totalPurchasedCredits,
        long successfulOrderCount,
        Instant firstPaidAt,
        Instant lastPaidAt,
        CreditWalletResponse wallet) {
}
