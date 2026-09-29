package fit.iuh.se.hschat.dto.request;

import fit.iuh.se.hschat.entity.enums.CareServiceCode;
import fit.iuh.se.hschat.entity.enums.CareServiceSupportPolicy;
import fit.iuh.se.hschat.entity.enums.DoctorSpecialty;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateCareServicePackageRequest {

    @NotBlank(message = "{validation.package-name-required}")
    @Size(max = 160, message = "{validation.package-name-max-160}")
    String name;

    @Size(max = 1000, message = "{validation.description-max-1000}")
    String description;

    @Size(max = 500, message = "{validation.short-description-max-500}")
    String shortDescription;

    @Size(max = 4000, message = "{validation.detailed-description-max-4000}")
    String detailedDescription;

    @NotNull(message = "{validation.package-price-required}")
    @DecimalMin(value = "0.01", message = "{validation.package-price-positive}")
    BigDecimal priceAmount;

    @Size(min = 3, max = 3, message = "{validation.currency-code-length-3}")
    String currency;

    @NotNull(message = "{validation.package-duration-required}")
    @Min(value = 1, message = "{validation.package-duration-positive}")
    Integer durationDays;

    List<CareServiceCode> includedServices;

    List<CareServiceCode> excludedServices;

    DoctorSpecialty requiredSpecialty;

    CareServiceSupportPolicy supportPolicy;

    @NotNull(message = "{validation.renewable-required}")
    Boolean renewable;

    @Size(max = 255, message = "{validation.terms-policy-reference-max-255}")
    String termsPolicyReference;
}
