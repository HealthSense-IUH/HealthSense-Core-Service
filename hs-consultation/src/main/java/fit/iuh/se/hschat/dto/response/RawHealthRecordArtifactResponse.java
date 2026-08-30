package fit.iuh.se.hschat.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RawHealthRecordArtifactResponse {
    Long sessionId;
    Long healthRecordId;
    String fileName;
    String downloadUrl;
}
