package fit.iuh.se.hshealthrecord.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PresignedUrlRequest {

    @NotBlank(message = "{validation.file-name-required}")
    String fileName;

    @NotNull(message = "{validation.file-size-required}")
    @Positive(message = "{validation.upload-file-size-positive}")
    Long fileSize;
}
