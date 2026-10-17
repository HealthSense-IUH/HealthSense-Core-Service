package fit.iuh.se.hschat.repository.statistics;

import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ConsultationStatisticsQueryRepositoryTest {

    @Test
    void bindsInstantRangeAsPostgresTimestampWithTimezone() {
        Instant from = Instant.parse("2026-09-30T17:00:00Z");
        Instant to = Instant.parse("2026-10-07T17:00:00Z");

        var parameters = ConsultationStatisticsQueryRepository.rangeParameters(from, to);

        assertEquals(Types.TIMESTAMP_WITH_TIMEZONE, parameters.getSqlType("from"));
        assertEquals(Types.TIMESTAMP_WITH_TIMEZONE, parameters.getSqlType("to"));
        assertInstanceOf(OffsetDateTime.class, parameters.getValue("from"));
        assertInstanceOf(OffsetDateTime.class, parameters.getValue("to"));
        assertEquals(from.atOffset(ZoneOffset.UTC), parameters.getValue("from"));
        assertEquals(to.atOffset(ZoneOffset.UTC), parameters.getValue("to"));
    }

    @Test
    void bindsMemberIdWithoutChangingTimestampTypes() {
        Instant from = Instant.parse("2026-09-30T17:00:00Z");
        Instant to = Instant.parse("2026-10-07T17:00:00Z");

        var parameters = ConsultationStatisticsQueryRepository.memberRangeParameters(123L, from, to);

        assertEquals(Types.BIGINT, parameters.getSqlType("memberId"));
        assertEquals(123L, parameters.getValue("memberId"));
        assertEquals(Types.TIMESTAMP_WITH_TIMEZONE, parameters.getSqlType("from"));
        assertEquals(Types.TIMESTAMP_WITH_TIMEZONE, parameters.getSqlType("to"));
    }
}
