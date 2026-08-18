package fit.iuh.se.hshealthrecord.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HealthStatisticsResponse {
    List<HealthStatItemResponse> chartData;
    int totalNormal;
    int totalAfibRisk;
    int totalUncertain;
    int totalAfibSuspected;
}
