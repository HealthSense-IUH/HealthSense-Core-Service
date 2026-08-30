package fit.iuh.se.hschat.dto.response;

import fit.iuh.se.hschat.entity.enums.CareServicePackageStatus;
import fit.iuh.se.hschat.entity.enums.CareServiceCode;
import fit.iuh.se.hschat.entity.enums.CareServiceSupportPolicy;
import fit.iuh.se.hschat.entity.enums.DoctorSpecialty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CareServicePackageResponse {

    Long id;
    Long familyId;
    String code;
    Integer version;
    String name;
    String shortDescription;
    String description;
    String detailedDescription;
    BigDecimal priceAmount;
    String currency;
    Integer durationDays;
    List<CareServiceCode> includedServices;
    List<CareServiceCode> excludedServices;
    DoctorSpecialty requiredSpecialty;
    CareServiceSupportPolicy supportPolicy;
    Boolean renewable;
    String termsPolicyReference;
    CareServicePackageStatus status;
    Instant createdAt;
    Instant updatedAt;
}
