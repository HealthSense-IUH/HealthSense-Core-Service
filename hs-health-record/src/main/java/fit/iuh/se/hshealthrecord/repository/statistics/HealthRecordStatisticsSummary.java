package fit.iuh.se.hshealthrecord.repository.statistics;

public record HealthRecordStatisticsSummary(
        long totalRecords,
        long recordsInPeriod,
        long uniqueUsersInPeriod,
        long afDetectedOrSuspectedInPeriod) {
}
