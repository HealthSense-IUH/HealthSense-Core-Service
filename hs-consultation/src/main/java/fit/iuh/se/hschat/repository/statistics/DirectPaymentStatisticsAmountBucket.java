package fit.iuh.se.hschat.repository.statistics;

import java.time.LocalDate;

public record DirectPaymentStatisticsAmountBucket(LocalDate bucketStart, long amountVnd) {
}
