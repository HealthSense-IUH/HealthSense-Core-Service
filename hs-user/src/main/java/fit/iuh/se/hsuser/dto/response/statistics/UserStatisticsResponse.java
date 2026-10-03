package fit.iuh.se.hsuser.dto.response.statistics;

import fit.iuh.se.hsshared.statistics.StatisticsCountBucketResponse;
import fit.iuh.se.hsshared.statistics.StatisticsPeriodResponse;

import java.util.List;

public record UserStatisticsResponse(
        StatisticsPeriodResponse period,
        Summary summary,
        List<StatisticsCountBucketResponse> registrationTrend,
        List<CategoryCount> byRole,
        List<CategoryCount> byStatus) {

    public record Summary(
            long totalUsers,
            long newUsers,
            long activeAccounts,
            long totalDoctors) {
    }

    public record CategoryCount(String key, long count) {
    }
}
