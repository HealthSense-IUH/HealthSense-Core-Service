package fit.iuh.se.hshealthrecord.dto.response.statistics;

import fit.iuh.se.hsshared.statistics.StatisticsCountBucketResponse;
import fit.iuh.se.hsshared.statistics.StatisticsPeriodResponse;

import java.time.Instant;
import java.util.List;

public record MemberHealthRecordStatisticsResponse(
        StatisticsPeriodResponse period,
        Summary summary,
        List<StatisticsCountBucketResponse> recordTrend,
        List<CategoryCount> resultDistribution,
        List<CategoryCount> statusDistribution,
        List<HourCount> recordedHourDistribution) {

    public record Summary(
            long totalRecords,
            long recordsInPeriod,
            long completedInPeriod,
            long failedInPeriod,
            long afDetectedOrSuspectedInPeriod,
            Instant latestRecordAt) {
    }

    public record CategoryCount(String key, long count) {
    }

    public record HourCount(int hour, long count) {
    }
}
