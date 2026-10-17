package fit.iuh.se.hsbilling.repository.statistics;

import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.time.Instant;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class MemberCreditStatisticsQueryRepositoryTest {

    @Test
    void bindsMemberAndRangeWithPostgresTypes() {
        Instant from = Instant.parse("2026-09-30T17:00:00Z");
        Instant to = Instant.parse("2026-10-07T17:00:00Z");

        var parameters = MemberCreditStatisticsQueryRepository.parameters(123L, from, to);

        assertEquals(Types.BIGINT, parameters.getSqlType("memberId"));
        assertEquals(123L, parameters.getValue("memberId"));
        assertEquals(Types.TIMESTAMP_WITH_TIMEZONE, parameters.getSqlType("from"));
        assertEquals(Types.TIMESTAMP_WITH_TIMEZONE, parameters.getSqlType("to"));
        assertInstanceOf(OffsetDateTime.class, parameters.getValue("from"));
        assertInstanceOf(OffsetDateTime.class, parameters.getValue("to"));
    }
}
