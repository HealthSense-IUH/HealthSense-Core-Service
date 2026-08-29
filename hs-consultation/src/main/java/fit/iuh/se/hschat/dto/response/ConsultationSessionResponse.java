package fit.iuh.se.hschat.dto.response;

import fit.iuh.se.hschat.entity.enums.ConsultationSourceType;
import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import fit.iuh.se.hschat.entity.enums.FinalSummaryClosureStatus;
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
    Boolean exceptionalOverride;
    String overrideReason;
    Long createdByAdminId;
    ConsultationSourceType sourceType;
    ConsultationStatus status;
    Instant startedAt;
    Instant activatedAt;
    Instant endsAt;
    Instant supportEndsAt;
    String supportScheduleSnapshotJson;
    String supportTimezoneSnapshot;
    Long packageId;
    Integer packageVersion;
    BigDecimal packagePriceSnapshot;
    Integer packageDurationDaysSnapshot;
    Instant completedAt;
    FinalSummaryClosureStatus summaryClosureStatus;
    Instant summaryDueAt;
    Instant summaryEscalatedAt;
    String summaryEscalationReason;
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
