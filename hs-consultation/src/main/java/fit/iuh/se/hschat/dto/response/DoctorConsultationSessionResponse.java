package fit.iuh.se.hschat.dto.response;

import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorConsultationSessionResponse {
    Long id;
    ConsultationStatus status;
    UserSummaryResponse member;
    Instant startedAt;
    Instant endsAt;
    Instant supportEndsAt;
    Long healthRecordId;
    String lastMessagePreview;
    Instant lastMessageAt;
    Long unreadCount;
    Long unresolvedAttentionCount;
}
