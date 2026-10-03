package fit.iuh.se.hschat.repository.statistics;

import java.time.LocalDate;

public record DirectPaymentStatisticsCountBucket(LocalDate bucketStart, long count) {
}
