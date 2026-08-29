package fit.iuh.se.hschat.dto.response;

import fit.iuh.se.hschat.entity.enums.EpisodeHealthRecordAuthorizationSource;
import fit.iuh.se.hshealthrecord.dto.response.HealthRecordResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorScopedHealthRecordResponse {
    HealthRecordResponse record;
    boolean initialAttachedRecord;
    EpisodeHealthRecordAuthorizationSource authorizationSource;
    ConsultationAttentionResponse attention;
}
