package fit.iuh.se.hschat.repository.statistics;

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
public class ConsultationStatisticsQueryRepository {

    private static final String SUMMARY_SQL = """
            SELECT COUNT(*) AS total_consultations,
                   COUNT(*) FILTER (
                       WHERE created_at >= :from AND created_at < :to
                   ) AS consultations_in_period,
                   COUNT(*) FILTER (
                       WHERE created_at >= :from AND created_at < :to AND status = 'COMPLETED'
                   ) AS completed_in_period,
                   COUNT(*) FILTER (
                       WHERE created_at >= :from AND created_at < :to AND status = 'CANCELLED'
                   ) AS cancelled_in_period
              FROM consultation_sessions
            """;

    private static final String STATUS_DISTRIBUTION_SQL = """
            SELECT status AS category_key, COUNT(*) AS category_count
              FROM consultation_sessions
             WHERE created_at >= :from AND created_at < :to
             GROUP BY status
             ORDER BY status
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public ConsultationStatisticsSummary summarize(Instant from, Instant to) {
        return jdbc.queryForObject(SUMMARY_SQL, rangeParameters(from, to), (resultSet, rowNumber) ->
                new ConsultationStatisticsSummary(
                        resultSet.getLong("total_consultations"),
                        resultSet.getLong("consultations_in_period"),
                        resultSet.getLong("completed_in_period"),
                        resultSet.getLong("cancelled_in_period")));
    }

    public List<ConsultationStatisticsBucket> consultationTrend(
            Instant from,
            Instant to,
            StatisticsGroupBy groupBy,
            String timezone) {
        String sql = """
                SELECT CAST(date_trunc('%s', created_at AT TIME ZONE :timezone) AS date) AS bucket_start,
                       COUNT(*) AS bucket_count
                  FROM consultation_sessions
                 WHERE created_at >= :from AND created_at < :to
                 GROUP BY 1
                 ORDER BY 1
                """.formatted(groupBy.sqlUnit());
        var parameters = rangeParameters(from, to).addValue("timezone", timezone);
        return jdbc.query(sql, parameters, (resultSet, rowNumber) ->
                new ConsultationStatisticsBucket(
                        resultSet.getObject("bucket_start", LocalDate.class),
                        resultSet.getLong("bucket_count")));
    }

    public List<ConsultationStatisticsCategoryCount> statusDistribution(Instant from, Instant to) {
        return jdbc.query(STATUS_DISTRIBUTION_SQL, rangeParameters(from, to), (resultSet, rowNumber) ->
                new ConsultationStatisticsCategoryCount(
                        resultSet.getString("category_key"),
                        resultSet.getLong("category_count")));
    }

    static MapSqlParameterSource rangeParameters(Instant from, Instant to) {
        return new MapSqlParameterSource()
                .addValue("from", from.atOffset(ZoneOffset.UTC), Types.TIMESTAMP_WITH_TIMEZONE)
                .addValue("to", to.atOffset(ZoneOffset.UTC), Types.TIMESTAMP_WITH_TIMEZONE);
    }
}
