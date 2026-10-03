package fit.iuh.se.hsuser.repository.statistics;

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
public class UserStatisticsQueryRepository {

    private static final String SUMMARY_SQL = """
            SELECT COUNT(*) AS total_users,
                   COUNT(*) FILTER (WHERE created_at >= :from AND created_at < :to) AS new_users,
                   COUNT(*) FILTER (WHERE status = 'ACTIVE') AS active_accounts,
                   COUNT(*) FILTER (WHERE role = 'DOCTOR') AS total_doctors
              FROM user_accounts
            """;

    private static final String ROLE_DISTRIBUTION_SQL = """
            SELECT role AS category_key, COUNT(*) AS category_count
              FROM user_accounts
             GROUP BY role
             ORDER BY role
            """;

    private static final String STATUS_DISTRIBUTION_SQL = """
            SELECT status AS category_key, COUNT(*) AS category_count
              FROM user_accounts
             GROUP BY status
             ORDER BY status
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public UserStatisticsSummary summarize(Instant from, Instant to) {
        var parameters = rangeParameters(from, to);
        return jdbc.queryForObject(SUMMARY_SQL, parameters, (resultSet, rowNumber) ->
                new UserStatisticsSummary(
                        resultSet.getLong("total_users"),
                        resultSet.getLong("new_users"),
                        resultSet.getLong("active_accounts"),
                        resultSet.getLong("total_doctors")));
    }

    public List<UserStatisticsBucket> registrationTrend(
            Instant from,
            Instant to,
            StatisticsGroupBy groupBy,
            String timezone) {
        String sql = """
                SELECT CAST(date_trunc('%s', created_at AT TIME ZONE :timezone) AS date) AS bucket_start,
                       COUNT(*) AS bucket_count
                  FROM user_accounts
                 WHERE created_at >= :from AND created_at < :to
                 GROUP BY 1
                 ORDER BY 1
                """.formatted(groupBy.sqlUnit());
        var parameters = rangeParameters(from, to)
                .addValue("timezone", timezone);
        return jdbc.query(sql, parameters, (resultSet, rowNumber) ->
                new UserStatisticsBucket(
                        resultSet.getObject("bucket_start", LocalDate.class),
                        resultSet.getLong("bucket_count")));
    }

    public List<UserStatisticsCategoryCount> roleDistribution() {
        return distribution(ROLE_DISTRIBUTION_SQL);
    }

    public List<UserStatisticsCategoryCount> statusDistribution() {
        return distribution(STATUS_DISTRIBUTION_SQL);
    }

    private List<UserStatisticsCategoryCount> distribution(String sql) {
        return jdbc.query(sql, new MapSqlParameterSource(), (resultSet, rowNumber) ->
                new UserStatisticsCategoryCount(
                        resultSet.getString("category_key"),
                        resultSet.getLong("category_count")));
    }

    static MapSqlParameterSource rangeParameters(Instant from, Instant to) {
        return new MapSqlParameterSource()
                .addValue("from", from.atOffset(ZoneOffset.UTC), Types.TIMESTAMP_WITH_TIMEZONE)
                .addValue("to", to.atOffset(ZoneOffset.UTC), Types.TIMESTAMP_WITH_TIMEZONE);
    }
}
