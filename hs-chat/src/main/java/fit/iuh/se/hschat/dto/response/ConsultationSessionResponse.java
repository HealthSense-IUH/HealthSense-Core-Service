package fit.iuh.se.hschat.dto.response;

import fit.iuh.se.hschat.entity.enums.ConsultationSourceType;
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
public class ConsultationSessionResponse {

    String id;
    Long memberId;
    Long doctorId;
    Long createdByAdminId;
    ConsultationSourceType sourceType;
    ConsultationStatus status;
    Instant startedAt;
    Instant endsAt;
    Instant supportEndsAt;
    Instant closedAt;
    String closeReason;
    Long healthRecordId;
    String requestId;
    String lastMessageId;
    String lastMessagePreview;
    Instant lastMessageAt;
    Long unreadCount;
    Instant createdAt;
    Instant updatedAt;
}
