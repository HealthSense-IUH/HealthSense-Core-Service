package fit.iuh.se.hshealthrecord.repository.statistics;

import java.time.Instant;

public record MemberHealthRecordStatisticsSummary(
        long totalRecords,
        long recordsInPeriod,
        long completedInPeriod,
        long failedInPeriod,
        long afDetectedOrSuspectedInPeriod,
        Instant latestRecordAt) {
}
