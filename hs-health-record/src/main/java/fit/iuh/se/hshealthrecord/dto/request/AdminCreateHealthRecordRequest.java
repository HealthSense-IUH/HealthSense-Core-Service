package fit.iuh.se.hshealthrecord.dto.request;

import fit.iuh.se.hshealthrecord.entity.enums.PredictionLabel;
import fit.iuh.se.hshealthrecord.entity.enums.RecordStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminCreateHealthRecordRequest {

    @NotNull(message = "Member id is required")
    Long memberId;

    @Size(max = 255, message = "File name must not exceed 255 characters")
    String fileName;

    @Size(max = 500, message = "S3 file key must not exceed 500 characters")
    String s3FileKey;

    @Positive(message = "File size must be greater than 0")
    Long fileSize;

    RecordStatus status;

    PredictionLabel predictionLabel;

    Double confidence;

    String hrvFeaturesJson;
}
