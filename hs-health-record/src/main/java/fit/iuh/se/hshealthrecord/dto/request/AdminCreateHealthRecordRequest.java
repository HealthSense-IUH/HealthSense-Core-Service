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

    @NotNull(message = "{validation.member-id-required}")
    Long memberId;

    @Size(max = 255, message = "{validation.file-name-max-255}")
    String fileName;

    @Size(max = 500, message = "{validation.s3-file-key-max-500}")
    String s3FileKey;

    @Positive(message = "{validation.file-size-positive}")
    Long fileSize;

    RecordStatus status;

    PredictionLabel predictionLabel;

    Double confidence;

    String hrvFeaturesJson;
}
