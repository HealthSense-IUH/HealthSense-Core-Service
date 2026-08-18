package fit.iuh.se.hshealthrecord.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HealthStatItemResponse {
    String label;
    int normalCount;
    int afibRiskCount;
    int uncertainCount;
    int afibSuspectedCount;
}
