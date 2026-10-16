package fit.iuh.se.hschat.dto.response.statistics;

import fit.iuh.se.hschat.entity.enums.EpisodeHealthRecordAuthorizationSource;
import fit.iuh.se.hshealthrecord.entity.enums.PredictionLabel;
import fit.iuh.se.hshealthrecord.entity.enums.RecordStatus;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.time.Instant;

public record DoctorPatientHealthRecordResponse(
        @JsonSerialize(using = ToStringSerializer.class)
        Long recordId,
        @JsonSerialize(using = ToStringSerializer.class)
        Long sessionId,
        String fileName,
        Long fileSize,
        RecordStatus status,
        PredictionLabel predictionLabel,
        Double confidence,
        Instant recordedAt,
        EpisodeHealthRecordAuthorizationSource authorizationSource,
        Instant authorizedAt) {
}
