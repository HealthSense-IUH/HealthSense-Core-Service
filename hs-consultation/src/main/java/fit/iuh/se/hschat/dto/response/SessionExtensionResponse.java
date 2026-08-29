package fit.iuh.se.hschat.dto.response;

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
public class SessionExtensionResponse {
    Long id;
    Long sessionId;
    Long renewalId;
    Long agreementId;
    Long paymentId;
    Instant previousEndsAt;
    Instant newEndsAt;
    Integer durationDays;
    Long packageId;
    Integer packageVersion;
    BigDecimal priceAmount;
    String currency;
    String supportScheduleSnapshotJson;
    String supportTimezoneSnapshot;
    Instant appliedAt;
}
