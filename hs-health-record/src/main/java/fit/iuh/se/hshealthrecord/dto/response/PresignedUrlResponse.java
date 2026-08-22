package fit.iuh.se.hshealthrecord.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PresignedUrlResponse {
    @JsonSerialize(using = ToStringSerializer.class)
    Long recordId;
    String uploadUrl;
    String s3Key;
}
