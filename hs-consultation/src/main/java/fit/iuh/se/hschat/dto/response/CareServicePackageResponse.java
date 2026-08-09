package fit.iuh.se.hschat.dto.response;

import fit.iuh.se.hschat.entity.enums.CareServicePackageStatus;
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
public class CareServicePackageResponse {

    Long id;
    String code;
    String name;
    String description;
    BigDecimal priceAmount;
    Integer durationDays;
    Boolean renewable;
    CareServicePackageStatus status;
    Instant createdAt;
    Instant updatedAt;
}
