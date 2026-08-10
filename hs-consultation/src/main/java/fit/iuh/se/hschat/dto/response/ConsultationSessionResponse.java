package fit.iuh.se.hschat.dto.response;

import fit.iuh.se.hschat.entity.enums.ConsultationSourceType;
import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConsultationSessionResponse {

    Long id;
    Long memberId;
    Long doctorId;
    Long createdByAdminId;
    ConsultationSourceType sourceType;
    ConsultationStatus status;
    Instant startedAt;
    Instant endsAt;
    Instant supportEndsAt;
    String supportScheduleSnapshotJson;
    String supportTimezoneSnapshot;
    Long packageId;
    BigDecimal packagePriceSnapshot;
    Integer packageDurationDaysSnapshot;
    Instant completedAt;
    fit.iuh.se.hschat.entity.enums.ConsultationCompletionReason completionReason;
    Instant closedAt;
    String closeReason;
    Long healthRecordId;
    Long requestId;
    String lastMessageId;
    String lastMessagePreview;
    Instant lastMessageAt;
    Long unreadCount;
    Instant createdAt;
    Instant updatedAt;
}
