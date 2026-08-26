package fit.iuh.se.hschat.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConsultationMoreInfoCycleResponse {

    Long id;
    String requestedItemsCategory;
    String coordinatorMessage;
    Long requestedBy;
    Instant requestedAt;
    String memberResponse;
    List<Long> responseHealthRecordIds;
    List<HealthRecordSummaryResponse> responseHealthRecords;
    Instant respondedAt;
}
