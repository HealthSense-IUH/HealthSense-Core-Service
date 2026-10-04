package fit.iuh.se.hschat.repository.statistics;

public record DirectPaymentStatisticsSummary(
        long totalRevenueVnd,
        long revenueInPeriodVnd,
        long totalBusinessTransactions,
        long totalPaidTransactions) {
}
