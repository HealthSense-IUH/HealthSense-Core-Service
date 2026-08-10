package fit.iuh.se.hschat.service.doctor.impl;

import fit.iuh.se.hsshared.advice.entity.AppException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeclaredSupportScheduleValidatorTest {

    DeclaredSupportScheduleValidator validator = new DeclaredSupportScheduleValidator();

    @Test
    void validWeeklySchedulePasses() {
        String schedule = """
                {
                  "weekly": [
                    {"dayOfWeek": "MONDAY", "start": "07:00", "end": "11:00"},
                    {"dayOfWeek": "MONDAY", "start": "13:00", "end": "18:00"}
                  ]
                }
                """;

        assertDoesNotThrow(() -> validator.validate(schedule, "Asia/Ho_Chi_Minh", true));
    }

    @Test
    void overlappingIntervalsFail() {
        String schedule = """
                {
                  "weekly": [
                    {"dayOfWeek": "MONDAY", "start": "07:00", "end": "11:00"},
                    {"dayOfWeek": "MONDAY", "start": "10:00", "end": "13:00"}
                  ]
                }
                """;

        assertThrows(AppException.class, () -> validator.validate(schedule, "Asia/Ho_Chi_Minh", true));
    }

    @Test
    void invalidTimezoneFails() {
        String schedule = """
                {"weekly": [{"dayOfWeek": "MONDAY", "start": "07:00", "end": "11:00"}]}
                """;

        assertThrows(AppException.class, () -> validator.validate(schedule, "Not/AZone", true));
    }

    @Test
    void invalidTimeRangeFails() {
        String schedule = """
                {"weekly": [{"dayOfWeek": "MONDAY", "start": "11:00", "end": "07:00"}]}
                """;

        assertThrows(AppException.class, () -> validator.validate(schedule, "Asia/Ho_Chi_Minh", true));
    }
}
