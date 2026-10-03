package fit.iuh.se.hsbilling.repository.statistics;

import java.time.LocalDate;

public record CreditPaymentStatisticsCountBucket(LocalDate bucketStart, long count) {
}
