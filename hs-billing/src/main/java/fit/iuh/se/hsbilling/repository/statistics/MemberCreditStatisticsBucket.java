package fit.iuh.se.hsbilling.repository.statistics;

import java.time.LocalDate;

public record MemberCreditStatisticsBucket(
        LocalDate bucketStart,
        long paidVnd,
        long purchasedCredits,
        long successfulOrders) {
}
