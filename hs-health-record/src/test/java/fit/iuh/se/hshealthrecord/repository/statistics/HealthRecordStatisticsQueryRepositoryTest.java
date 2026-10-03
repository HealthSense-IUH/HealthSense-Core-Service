package fit.iuh.se.hshealthrecord.repository.statistics;

import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class HealthRecordStatisticsQueryRepositoryTest {

    @Test
    void bindsInstantRangeAsPostgresTimestampWithTimezone() {
        Instant from = Instant.parse("2026-09-30T17:00:00Z");
        Instant to = Instant.parse("2026-10-07T17:00:00Z");

        var parameters = HealthRecordStatisticsQueryRepository.rangeParameters(from, to);

        assertEquals(Types.TIMESTAMP_WITH_TIMEZONE, parameters.getSqlType("from"));
        assertEquals(Types.TIMESTAMP_WITH_TIMEZONE, parameters.getSqlType("to"));
        assertInstanceOf(OffsetDateTime.class, parameters.getValue("from"));
        assertInstanceOf(OffsetDateTime.class, parameters.getValue("to"));
        assertEquals(from.atOffset(ZoneOffset.UTC), parameters.getValue("from"));
        assertEquals(to.atOffset(ZoneOffset.UTC), parameters.getValue("to"));
    }
}
