package fit.iuh.se.hschat.dto.response.statistics;

import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import fit.iuh.se.hschat.entity.enums.FinalSummaryClosureStatus;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.time.Instant;

public record DoctorPatientConsultationResponse(
        @JsonSerialize(using = ToStringSerializer.class)
        Long sessionId,
        ConsultationStatus status,
        Instant createdAt,
        Instant startedAt,
        Instant activatedAt,
        Instant endsAt,
        Instant completedAt,
        FinalSummaryClosureStatus summaryClosureStatus) {
}
