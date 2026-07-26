package fit.iuh.se.hshealthrecord.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PresignedUrlResponse {

    Long recordId;
    String uploadUrl;
    String s3Key;
}
