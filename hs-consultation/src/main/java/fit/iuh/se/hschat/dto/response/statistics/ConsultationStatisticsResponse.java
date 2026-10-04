package fit.iuh.se.hschat.dto.response.statistics;

import fit.iuh.se.hsshared.statistics.StatisticsCountBucketResponse;
import fit.iuh.se.hsshared.statistics.StatisticsPeriodResponse;

import java.util.List;

public record ConsultationStatisticsResponse(
        StatisticsPeriodResponse period,
        Summary summary,
        List<StatisticsCountBucketResponse> consultationTrend,
        List<CategoryCount> byStatus) {

    public record Summary(
            long totalConsultations,
            long consultationsInPeriod,
            long completedInPeriod,
            long cancelledInPeriod) {
    }

    public record CategoryCount(String key, long count) {
    }
}
