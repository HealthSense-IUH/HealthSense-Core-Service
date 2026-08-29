package fit.iuh.se.hschat.dto.response;

import fit.iuh.se.hschat.entity.enums.EpisodeHealthRecordAuthorizationSource;
import fit.iuh.se.hschat.entity.enums.EpisodeHealthRecordAuthorizedByType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EpisodeHealthRecordAuthorizationResponse {
    Long id;
    Long sessionId;
    Long healthRecordId;
    EpisodeHealthRecordAuthorizationSource source;
    Instant authorizedAt;
    Long authorizedBy;
    EpisodeHealthRecordAuthorizedByType authorizedByType;
}
