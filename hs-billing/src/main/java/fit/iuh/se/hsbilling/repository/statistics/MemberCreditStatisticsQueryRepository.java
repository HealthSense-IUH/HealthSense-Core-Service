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
public class MemberCreditStatisticsQueryRepository {

    private static final String SUMMARY_SQL = """
            SELECT COALESCE(SUM(amount_vnd) FILTER (
                       WHERE status = 'PAID' AND currency = 'VND'
                   ), 0) AS total_paid_vnd,
                   COALESCE(SUM(amount_vnd) FILTER (
                       WHERE status = 'PAID' AND currency = 'VND'
                         AND paid_at >= :from AND paid_at < :to
                   ), 0) AS paid_in_period_vnd,
                   COALESCE(SUM(credit_quantity) FILTER (
                       WHERE status = 'PAID'
                   ), 0) AS total_purchased_credits,
                   COALESCE(SUM(credit_quantity) FILTER (
                       WHERE status = 'PAID' AND paid_at >= :from AND paid_at < :to
                   ), 0) AS purchased_credits_in_period,
                   COUNT(*) FILTER (WHERE status = 'PAID') AS total_successful_orders,
                   COUNT(*) FILTER (
                       WHERE status = 'PAID' AND paid_at >= :from AND paid_at < :to
                   ) AS successful_orders_in_period,
                   COUNT(*) FILTER (
                       WHERE created_at >= :from AND created_at < :to
                   ) AS total_orders_in_period
              FROM credit_purchase_orders
             WHERE member_id = :memberId
            """;

    private static final String STATUS_DISTRIBUTION_SQL = """
            SELECT status AS category_key, COUNT(*) AS category_count
              FROM credit_purchase_orders
             WHERE member_id = :memberId
               AND created_at >= :from AND created_at < :to
             GROUP BY status
             ORDER BY status
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public MemberCreditStatisticsSummary summarize(Long memberId, Instant from, Instant to) {
        return jdbc.queryForObject(SUMMARY_SQL, parameters(memberId, from, to), (resultSet, rowNumber) ->
                new MemberCreditStatisticsSummary(
                        resultSet.getLong("total_paid_vnd"),
                        resultSet.getLong("paid_in_period_vnd"),
                        resultSet.getLong("total_purchased_credits"),
                        resultSet.getLong("purchased_credits_in_period"),
                        resultSet.getLong("total_successful_orders"),
                        resultSet.getLong("successful_orders_in_period"),
                        resultSet.getLong("total_orders_in_period")));
    }

    public List<MemberCreditStatisticsBucket> paymentTrend(
            Long memberId,
            Instant from,
            Instant to,
            StatisticsGroupBy groupBy,
            String timezone) {
        String sql = """
                SELECT CAST(date_trunc('%s', paid_at AT TIME ZONE :timezone) AS date) AS bucket_start,
                       COALESCE(SUM(amount_vnd), 0) AS paid_vnd,
                       COALESCE(SUM(credit_quantity), 0) AS purchased_credits,
                       COUNT(*) AS successful_orders
                  FROM credit_purchase_orders
                 WHERE member_id = :memberId
                   AND status = 'PAID'
                   AND paid_at >= :from AND paid_at < :to
                 GROUP BY 1
                 ORDER BY 1
                """.formatted(groupBy.sqlUnit());
        var parameters = parameters(memberId, from, to).addValue("timezone", timezone);
        return jdbc.query(sql, parameters, (resultSet, rowNumber) -> new MemberCreditStatisticsBucket(
                resultSet.getObject("bucket_start", LocalDate.class),
                resultSet.getLong("paid_vnd"),
                resultSet.getLong("purchased_credits"),
                resultSet.getLong("successful_orders")));
    }

    public List<CreditPaymentStatisticsCategoryCount> statusDistribution(
            Long memberId,
            Instant from,
            Instant to) {
        return jdbc.query(STATUS_DISTRIBUTION_SQL, parameters(memberId, from, to), (resultSet, rowNumber) ->
                new CreditPaymentStatisticsCategoryCount(
                        resultSet.getString("category_key"),
                        resultSet.getLong("category_count")));
    }

    static MapSqlParameterSource parameters(Long memberId, Instant from, Instant to) {
        return new MapSqlParameterSource()
                .addValue("memberId", memberId, Types.BIGINT)
                .addValue("from", from.atOffset(ZoneOffset.UTC), Types.TIMESTAMP_WITH_TIMEZONE)
                .addValue("to", to.atOffset(ZoneOffset.UTC), Types.TIMESTAMP_WITH_TIMEZONE);
    }
}
