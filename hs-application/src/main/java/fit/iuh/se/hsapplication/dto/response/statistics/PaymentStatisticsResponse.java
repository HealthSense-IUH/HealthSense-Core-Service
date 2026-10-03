package fit.iuh.se.hsapplication.dto.response.statistics;

import fit.iuh.se.hsshared.statistics.StatisticsCountBucketResponse;
import fit.iuh.se.hsshared.statistics.StatisticsPeriodResponse;

import java.time.LocalDate;
import java.util.List;

public record PaymentStatisticsResponse(
        StatisticsPeriodResponse period,
        Summary summary,
        List<RevenueBucket> revenueTrend,
        List<StatisticsCountBucketResponse> transactionTrend,
        StatusDistributionBySource statusDistributionBySource,
        List<RevenueBySource> revenueBySource) {

    public record Summary(
            long totalRevenueVnd,
            long revenueInPeriodVnd,
            long totalBusinessTransactions,
            long averagePaidTransactionValueVnd) {
    }

    public record RevenueBucket(
            LocalDate bucketStart,
            long totalVnd,
            long directCareVnd,
            long creditPurchaseVnd) {
    }

    public record CategoryCount(String key, long count) {
    }

    public record StatusDistributionBySource(
            List<CategoryCount> directCare,
            List<CategoryCount> creditPurchase) {
    }

    public record RevenueBySource(String key, long amountVnd) {
    }
}
