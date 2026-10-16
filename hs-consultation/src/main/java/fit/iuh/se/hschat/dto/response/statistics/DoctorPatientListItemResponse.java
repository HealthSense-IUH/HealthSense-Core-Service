package fit.iuh.se.hschat.dto.response.statistics;

import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.time.Instant;

public record DoctorPatientListItemResponse(
        @JsonSerialize(using = ToStringSerializer.class)
        Long memberId,
        String displayName,
        String avatarUrl,
        long totalConsultations,
        long completedConsultations,
        Instant firstConsultationAt,
        Instant latestConsultationAt,
        ConsultationStatus latestConsultationStatus) {
}
