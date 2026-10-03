package fit.iuh.se.hshealthrecord.dto.response.statistics;

import fit.iuh.se.hsshared.statistics.StatisticsCountBucketResponse;
import fit.iuh.se.hsshared.statistics.StatisticsPeriodResponse;

import java.util.List;

public record HealthRecordStatisticsResponse(
        StatisticsPeriodResponse period,
        Summary summary,
        List<StatisticsCountBucketResponse> recordTrend,
        List<CategoryCount> resultDistribution,
        List<HourCount> recordedHourDistribution) {

    public record Summary(
            long totalRecords,
            long recordsInPeriod,
            long uniqueUsersInPeriod,
            long afDetectedOrSuspectedInPeriod) {
    }

    public record CategoryCount(String key, long count) {
    }

    public record HourCount(int hour, long count) {
    }
}
