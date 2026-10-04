package fit.iuh.se.hsbilling.repository.statistics;

public record CreditPaymentStatisticsSummary(
        long totalRevenueVnd,
        long revenueInPeriodVnd,
        long totalBusinessTransactions,
        long totalPaidTransactions) {
}
