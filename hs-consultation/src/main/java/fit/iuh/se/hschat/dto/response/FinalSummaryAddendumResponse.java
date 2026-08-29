package fit.iuh.se.hschat.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FinalSummaryAddendumResponse {
    Long id;
    Long summaryId;
    Long sessionId;
    Long authorDoctorId;
    String reason;
    String content;
    Instant authoredAt;
}
