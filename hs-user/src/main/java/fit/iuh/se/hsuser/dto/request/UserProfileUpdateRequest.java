package fit.iuh.se.hsuser.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfileUpdateRequest {

    @Size(max = 120, message = "{validation.display-name-max-120}")
    String displayName;

    @Size(max = 30, message = "{validation.phone-max-30}")
    String phone;

    LocalDate dateOfBirth;

    @Size(max = 20, message = "{validation.gender-max-20}")
    String gender;

    @Size(max = 500, message = "{validation.address-max-500}")
    String address;

    @Size(max = 50, message = "{validation.timezone-max-50}")
    String timezone;

    @Size(max = 500, message = "{validation.avatar-url-max-500}")
    String avatarUrl;

    @Size(max = 20, message = "{validation.citizen-id-max-20}")
    String citizenId;

    @Size(max = 100, message = "{validation.bank-account-max-100}")
    String bankAccount;

    @Size(max = 50, message = "{validation.health-insurance-number-max-50}")
    String healthInsuranceNumber;

    String healthData;

    String biometricData;

    @Size(max = 500, message = "{validation.identity-card-front-url-max-500}")
    String identityCardFrontUrl;

    @Size(max = 500, message = "{validation.identity-card-back-url-max-500}")
    String identityCardBackUrl;

    Integer identityCardFrontRotate;

    Integer identityCardBackRotate;
}
