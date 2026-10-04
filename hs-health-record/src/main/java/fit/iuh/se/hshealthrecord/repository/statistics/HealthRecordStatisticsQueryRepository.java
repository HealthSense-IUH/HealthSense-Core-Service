package fit.iuh.se.hshealthrecord.repository.statistics;

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
public class HealthRecordStatisticsQueryRepository {

    private static final String SUMMARY_SQL = """
            SELECT COUNT(*) AS total_records,
                   COUNT(*) FILTER (
                       WHERE created_at >= :from AND created_at < :to
                   ) AS records_in_period,
                   COUNT(DISTINCT user_id) FILTER (
                       WHERE created_at >= :from AND created_at < :to
                   ) AS unique_users_in_period,
                   COUNT(*) FILTER (
                       WHERE created_at >= :from AND created_at < :to
                         AND status = 'COMPLETED'
                         AND prediction_label IN ('AFIB', 'AFIB_SUSPECTED')
                   ) AS af_detected_or_suspected_in_period
              FROM health_records
            """;

    private static final String RESULT_DISTRIBUTION_SQL = """
            SELECT prediction_label AS category_key, COUNT(*) AS category_count
              FROM health_records
             WHERE created_at >= :from AND created_at < :to
               AND status = 'COMPLETED'
               AND prediction_label IS NOT NULL
             GROUP BY prediction_label
             ORDER BY prediction_label
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public HealthRecordStatisticsSummary summarize(Instant from, Instant to) {
        return jdbc.queryForObject(SUMMARY_SQL, rangeParameters(from, to), (resultSet, rowNumber) ->
                new HealthRecordStatisticsSummary(
                        resultSet.getLong("total_records"),
                        resultSet.getLong("records_in_period"),
                        resultSet.getLong("unique_users_in_period"),
                        resultSet.getLong("af_detected_or_suspected_in_period")));
    }

    public List<HealthRecordStatisticsBucket> recordTrend(
            Instant from,
            Instant to,
            StatisticsGroupBy groupBy,
            String timezone) {
        String sql = """
                SELECT CAST(date_trunc('%s', created_at AT TIME ZONE :timezone) AS date) AS bucket_start,
                       COUNT(*) AS bucket_count
                  FROM health_records
                 WHERE created_at >= :from AND created_at < :to
                 GROUP BY 1
                 ORDER BY 1
                """.formatted(groupBy.sqlUnit());
        var parameters = rangeParameters(from, to).addValue("timezone", timezone);
        return jdbc.query(sql, parameters, (resultSet, rowNumber) ->
                new HealthRecordStatisticsBucket(
                        resultSet.getObject("bucket_start", LocalDate.class),
                        resultSet.getLong("bucket_count")));
    }

    public List<HealthRecordStatisticsCategoryCount> resultDistribution(Instant from, Instant to) {
        return jdbc.query(RESULT_DISTRIBUTION_SQL, rangeParameters(from, to), (resultSet, rowNumber) ->
                new HealthRecordStatisticsCategoryCount(
                        resultSet.getString("category_key"),
                        resultSet.getLong("category_count")));
    }

    public List<HealthRecordStatisticsHourCount> recordedHourDistribution(
            Instant from,
            Instant to,
            String timezone) {
        String sql = """
                SELECT CAST(EXTRACT(HOUR FROM created_at AT TIME ZONE :timezone) AS integer) AS recorded_hour,
                       COUNT(*) AS hour_count
                  FROM health_records
                 WHERE created_at >= :from AND created_at < :to
                 GROUP BY 1
                 ORDER BY 1
                """;
        var parameters = rangeParameters(from, to).addValue("timezone", timezone);
        return jdbc.query(sql, parameters, (resultSet, rowNumber) ->
                new HealthRecordStatisticsHourCount(
                        resultSet.getInt("recorded_hour"),
                        resultSet.getLong("hour_count")));
    }

    static MapSqlParameterSource rangeParameters(Instant from, Instant to) {
        return new MapSqlParameterSource()
                .addValue("from", from.atOffset(ZoneOffset.UTC), Types.TIMESTAMP_WITH_TIMEZONE)
                .addValue("to", to.atOffset(ZoneOffset.UTC), Types.TIMESTAMP_WITH_TIMEZONE);
    }
}
