package fit.iuh.se.hsbilling.repository.statistics;

import fit.iuh.se.hsshared.statistics.StatisticsGroupBy;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CreditPaymentStatisticsQueryRepository {

    private static final String PAYOS_ATTEMPT_EXISTS = """
            EXISTS (
                SELECT 1
                  FROM credit_payment_attempts attempt
                 WHERE attempt.order_id = orders.id
                   AND attempt.provider = 'PAYOS'
            )
            """;

    private static final String PAID_PAYOS_ATTEMPT_EXISTS = """
            EXISTS (
                SELECT 1
                  FROM credit_payment_attempts attempt
                 WHERE attempt.order_id = orders.id
                   AND attempt.provider = 'PAYOS'
                   AND attempt.status = 'PAID'
            )
            """;

    private static final String SUMMARY_SQL = """
            SELECT COALESCE(SUM(amount_vnd) FILTER (
                       WHERE status = 'PAID' AND currency = 'VND' AND %s
                   ), 0) AS total_revenue_vnd,
                   COALESCE(SUM(amount_vnd) FILTER (
                       WHERE status = 'PAID' AND currency = 'VND' AND %s
                         AND paid_at >= :from AND paid_at < :to
                   ), 0) AS revenue_in_period_vnd,
                   COUNT(*) FILTER (WHERE %s) AS total_business_transactions,
                   COUNT(*) FILTER (
                       WHERE status = 'PAID' AND currency = 'VND' AND %s
                   ) AS total_paid_transactions
              FROM credit_purchase_orders orders
            """.formatted(
            PAID_PAYOS_ATTEMPT_EXISTS,
            PAID_PAYOS_ATTEMPT_EXISTS,
            PAYOS_ATTEMPT_EXISTS,
            PAID_PAYOS_ATTEMPT_EXISTS);

    private static final String STATUS_DISTRIBUTION_SQL = """
            SELECT status AS category_key, COUNT(*) AS category_count
              FROM credit_purchase_orders orders
             WHERE created_at >= :from AND created_at < :to
               AND %s
             GROUP BY status
             ORDER BY status
            """.formatted(PAYOS_ATTEMPT_EXISTS);

    private final NamedParameterJdbcTemplate jdbc;

    public CreditPaymentStatisticsSummary summarize(Instant from, Instant to) {
        return jdbc.queryForObject(SUMMARY_SQL, rangeParameters(from, to), (resultSet, rowNumber) ->
                new CreditPaymentStatisticsSummary(
                        resultSet.getLong("total_revenue_vnd"),
                        resultSet.getLong("revenue_in_period_vnd"),
                        resultSet.getLong("total_business_transactions"),
                        resultSet.getLong("total_paid_transactions")));
    }

    public List<CreditPaymentStatisticsAmountBucket> revenueTrend(
            Instant from,
            Instant to,
            StatisticsGroupBy groupBy,
            String timezone) {
        String sql = """
                SELECT CAST(date_trunc('%s', paid_at AT TIME ZONE :timezone) AS date) AS bucket_start,
                       COALESCE(SUM(amount_vnd), 0) AS amount_vnd
                  FROM credit_purchase_orders orders
                 WHERE status = 'PAID'
                   AND currency = 'VND'
                   AND paid_at >= :from AND paid_at < :to
                   AND %s
                 GROUP BY 1
                 ORDER BY 1
                """.formatted(groupBy.sqlUnit(), PAID_PAYOS_ATTEMPT_EXISTS);
        var parameters = rangeParameters(from, to).addValue("timezone", timezone);
        return jdbc.query(sql, parameters, (resultSet, rowNumber) ->
                new CreditPaymentStatisticsAmountBucket(
                        resultSet.getObject("bucket_start", LocalDate.class),
                        resultSet.getLong("amount_vnd")));
    }

    public List<CreditPaymentStatisticsCountBucket> transactionTrend(
            Instant from,
            Instant to,
            StatisticsGroupBy groupBy,
            String timezone) {
        String sql = """
                SELECT CAST(date_trunc('%s', created_at AT TIME ZONE :timezone) AS date) AS bucket_start,
                       COUNT(*) AS bucket_count
                  FROM credit_purchase_orders orders
                 WHERE created_at >= :from AND created_at < :to
                   AND %s
                 GROUP BY 1
                 ORDER BY 1
                """.formatted(groupBy.sqlUnit(), PAYOS_ATTEMPT_EXISTS);
        var parameters = rangeParameters(from, to).addValue("timezone", timezone);
        return jdbc.query(sql, parameters, (resultSet, rowNumber) ->
                new CreditPaymentStatisticsCountBucket(
                        resultSet.getObject("bucket_start", LocalDate.class),
                        resultSet.getLong("bucket_count")));
    }

    public List<CreditPaymentStatisticsCategoryCount> statusDistribution(Instant from, Instant to) {
        return jdbc.query(STATUS_DISTRIBUTION_SQL, rangeParameters(from, to), (resultSet, rowNumber) ->
                new CreditPaymentStatisticsCategoryCount(
                        resultSet.getString("category_key"),
                        resultSet.getLong("category_count")));
    }

    static MapSqlParameterSource rangeParameters(Instant from, Instant to) {
        return new MapSqlParameterSource()
                .addValue("from", from.atOffset(ZoneOffset.UTC), Types.TIMESTAMP_WITH_TIMEZONE)
                .addValue("to", to.atOffset(ZoneOffset.UTC), Types.TIMESTAMP_WITH_TIMEZONE);
    }
}
