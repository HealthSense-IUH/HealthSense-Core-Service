package fit.iuh.se.hschat.dto.response.statistics;

import java.time.Instant;
import java.util.Set;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

public record DoctorPatientMedicalHistoryResponse(
        @JsonSerialize(using = ToStringSerializer.class)
        Long summaryId,
        @JsonSerialize(using = ToStringSerializer.class)
        Long sessionId,
        String summary,
        String observations,
        String recommendations,
        String followUpRecommendation,
        Instant finalizedAt,
        @JsonSerialize(contentUsing = ToStringSerializer.class)
        Set<Long> referencedHealthRecordIds) {
}
