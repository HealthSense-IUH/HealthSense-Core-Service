package fit.iuh.se.hsuser.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AvatarPresignedUrlResponse {

    String uploadUrl;
    String s3Key;
    String publicUrl;
}
