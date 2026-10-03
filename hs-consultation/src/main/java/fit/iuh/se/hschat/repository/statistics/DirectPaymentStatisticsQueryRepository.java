package fit.iuh.se.hschat.repository.statistics;

import fit.iuh.se.hsshared.statistics.StatisticsGroupBy;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DirectPaymentStatisticsQueryRepository {

    private static final String SUMMARY_SQL = """
            SELECT COALESCE(SUM(amount) FILTER (
                       WHERE status = 'PAID' AND currency = 'VND'
                   ), 0) AS total_revenue_vnd,
                   COALESCE(SUM(amount) FILTER (
                       WHERE status = 'PAID' AND currency = 'VND'
                         AND paid_at >= :from AND paid_at < :to
                   ), 0) AS revenue_in_period_vnd,
                   COUNT(*) AS total_business_transactions,
                   COUNT(*) FILTER (
                       WHERE status = 'PAID' AND currency = 'VND'
                   ) AS total_paid_transactions
              FROM consultation_payments
             WHERE provider = 'PAYOS'
            """;

    private static final String STATUS_DISTRIBUTION_SQL = """
            SELECT status AS category_key, COUNT(*) AS category_count
              FROM consultation_payments
             WHERE provider = 'PAYOS'
               AND created_at >= :from AND created_at < :to
             GROUP BY status
             ORDER BY status
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public DirectPaymentStatisticsSummary summarize(Instant from, Instant to) {
        return jdbc.queryForObject(SUMMARY_SQL, rangeParameters(from, to), (resultSet, rowNumber) ->
                new DirectPaymentStatisticsSummary(
                        vnd(resultSet.getBigDecimal("total_revenue_vnd")),
                        vnd(resultSet.getBigDecimal("revenue_in_period_vnd")),
                        resultSet.getLong("total_business_transactions"),
                        resultSet.getLong("total_paid_transactions")));
    }

    public List<DirectPaymentStatisticsAmountBucket> revenueTrend(
            Instant from,
            Instant to,
            StatisticsGroupBy groupBy,
            String timezone) {
        String sql = """
                SELECT CAST(date_trunc('%s', paid_at AT TIME ZONE :timezone) AS date) AS bucket_start,
                       COALESCE(SUM(amount), 0) AS amount_vnd
                  FROM consultation_payments
                 WHERE provider = 'PAYOS'
                   AND status = 'PAID'
                   AND currency = 'VND'
                   AND paid_at >= :from AND paid_at < :to
                 GROUP BY 1
                 ORDER BY 1
                """.formatted(groupBy.sqlUnit());
        var parameters = rangeParameters(from, to).addValue("timezone", timezone);
        return jdbc.query(sql, parameters, (resultSet, rowNumber) ->
                new DirectPaymentStatisticsAmountBucket(
                        resultSet.getObject("bucket_start", LocalDate.class),
                        vnd(resultSet.getBigDecimal("amount_vnd"))));
    }

    public List<DirectPaymentStatisticsCountBucket> transactionTrend(
            Instant from,
            Instant to,
            StatisticsGroupBy groupBy,
            String timezone) {
        String sql = """
                SELECT CAST(date_trunc('%s', created_at AT TIME ZONE :timezone) AS date) AS bucket_start,
                       COUNT(*) AS bucket_count
                  FROM consultation_payments
                 WHERE provider = 'PAYOS'
                   AND created_at >= :from AND created_at < :to
                 GROUP BY 1
                 ORDER BY 1
                """.formatted(groupBy.sqlUnit());
        var parameters = rangeParameters(from, to).addValue("timezone", timezone);
        return jdbc.query(sql, parameters, (resultSet, rowNumber) ->
                new DirectPaymentStatisticsCountBucket(
                        resultSet.getObject("bucket_start", LocalDate.class),
                        resultSet.getLong("bucket_count")));
    }

    public List<DirectPaymentStatisticsCategoryCount> statusDistribution(Instant from, Instant to) {
        return jdbc.query(STATUS_DISTRIBUTION_SQL, rangeParameters(from, to), (resultSet, rowNumber) ->
                new DirectPaymentStatisticsCategoryCount(
                        resultSet.getString("category_key"),
                        resultSet.getLong("category_count")));
    }

    static MapSqlParameterSource rangeParameters(Instant from, Instant to) {
        return new MapSqlParameterSource()
                .addValue("from", from.atOffset(ZoneOffset.UTC), Types.TIMESTAMP_WITH_TIMEZONE)
                .addValue("to", to.atOffset(ZoneOffset.UTC), Types.TIMESTAMP_WITH_TIMEZONE);
    }

    private static long vnd(BigDecimal value) {
        return value == null ? 0L : value.longValueExact();
    }
}
