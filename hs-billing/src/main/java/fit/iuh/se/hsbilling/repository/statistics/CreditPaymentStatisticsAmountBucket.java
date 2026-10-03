package fit.iuh.se.hsbilling.repository.statistics;

import java.time.LocalDate;

public record CreditPaymentStatisticsAmountBucket(LocalDate bucketStart, long amountVnd) {
}
