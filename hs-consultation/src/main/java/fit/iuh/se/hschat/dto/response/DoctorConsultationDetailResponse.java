package fit.iuh.se.hschat.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorConsultationDetailResponse {
    ConsultationSessionResponse session;
    UserSummaryResponse member;
    DoctorScopedHealthRecordResponse initialHealthRecord;
    Long unresolvedAttentionCount;
}
