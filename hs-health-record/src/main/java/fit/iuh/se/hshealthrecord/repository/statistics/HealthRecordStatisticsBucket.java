package fit.iuh.se.hshealthrecord.repository.statistics;

import java.time.LocalDate;

public record HealthRecordStatisticsBucket(LocalDate bucketStart, long count) {
}
