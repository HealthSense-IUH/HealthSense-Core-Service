package fit.iuh.se.hsshared.statistics;

import java.time.LocalDate;

public record StatisticsCountBucketResponse(LocalDate bucketStart, long count) {
}
