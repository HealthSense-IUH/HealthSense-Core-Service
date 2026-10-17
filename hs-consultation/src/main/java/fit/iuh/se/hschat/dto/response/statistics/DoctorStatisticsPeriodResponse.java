package fit.iuh.se.hschat.dto.response.statistics;

import java.time.Instant;

public record DoctorStatisticsPeriodResponse(Instant from, Instant to) {
}
