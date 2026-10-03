package fit.iuh.se.hsbilling.repository.statistics;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.Types;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreditPaymentStatisticsQueryRepositoryTest {

    @Test
    void bindsInstantRangeAsPostgresTimestampWithTimezone() {
        Instant from = Instant.parse("2026-09-30T17:00:00Z");
        Instant to = Instant.parse("2026-10-07T17:00:00Z");

        var parameters = CreditPaymentStatisticsQueryRepository.rangeParameters(from, to);

        assertEquals(Types.TIMESTAMP_WITH_TIMEZONE, parameters.getSqlType("from"));
        assertEquals(Types.TIMESTAMP_WITH_TIMEZONE, parameters.getSqlType("to"));
        assertInstanceOf(OffsetDateTime.class, parameters.getValue("from"));
        assertInstanceOf(OffsetDateTime.class, parameters.getValue("to"));
        assertEquals(from.atOffset(ZoneOffset.UTC), parameters.getValue("from"));
        assertEquals(to.atOffset(ZoneOffset.UTC), parameters.getValue("to"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void summaryUsesCorrelatedExistsInsteadOfJoiningAttempts() {
        var jdbc = mock(NamedParameterJdbcTemplate.class);
        var expected = new CreditPaymentStatisticsSummary(10, 5, 2, 1);
        when(jdbc.queryForObject(
                anyString(),
                any(MapSqlParameterSource.class),
                any(RowMapper.class))).thenReturn(expected);
        var repository = new CreditPaymentStatisticsQueryRepository(jdbc);

        repository.summarize(
                Instant.parse("2026-09-01T00:00:00Z"),
                Instant.parse("2026-10-01T00:00:00Z"));

        var sqlCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(jdbc).queryForObject(
                sqlCaptor.capture(),
                any(MapSqlParameterSource.class),
                any(RowMapper.class));
        String normalizedSql = sqlCaptor.getValue().toLowerCase();
        assertTrue(normalizedSql.contains("exists ("));
        assertTrue(normalizedSql.contains("attempt.provider = 'payos'"));
        assertTrue(normalizedSql.contains("attempt.status = 'paid'"));
        assertFalse(normalizedSql.contains(" join credit_payment_attempts"));
    }
}
