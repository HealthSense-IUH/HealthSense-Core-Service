package fit.iuh.se.hshealthrecord.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HealthRecordAvailableForCareEvent {
    Long recordId;
    Long memberId;
    Instant availableAt;
}
