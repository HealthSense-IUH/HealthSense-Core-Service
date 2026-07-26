package fit.iuh.se.hshealthrecord.dto.response;

import fit.iuh.se.hshealthrecord.entity.enums.PredictionLabel;
import fit.iuh.se.hshealthrecord.entity.enums.RecordStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HealthRecordResponse {

    Long id;
    Long userId;
    String fileName;
    Long fileSize;
    RecordStatus status;
    PredictionLabel predictionLabel;
    Double confidence;
    String hrvFeaturesJson;
    Instant createdAt;
    Instant updatedAt;
}
