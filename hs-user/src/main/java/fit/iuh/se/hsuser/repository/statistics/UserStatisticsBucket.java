package fit.iuh.se.hsuser.repository.statistics;

import java.time.LocalDate;

public record UserStatisticsBucket(LocalDate bucketStart, long count) {
}
