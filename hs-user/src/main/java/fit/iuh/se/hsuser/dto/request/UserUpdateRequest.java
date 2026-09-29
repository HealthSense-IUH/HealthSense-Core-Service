package fit.iuh.se.hsuser.dto.request;

import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import jakarta.validation.constraints.Email;
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
public class UserUpdateRequest {

    @Email(message = "{validation.email-invalid}")
    @Size(max = 255, message = "{validation.email-max-255}")
    String email;

    UserRole role;

    AccountStatus status;

    @Size(max = 120, message = "{validation.display-name-max-120}")
    String displayName;

    @Size(max = 30, message = "{validation.phone-max-30}")
    String phone;

    LocalDate dateOfBirth;

    @Size(max = 20, message = "{validation.gender-max-20}")
    String gender;

    @Size(max = 500, message = "{validation.address-max-500}")
    String address;

    @Size(max = 20, message = "{validation.citizen-id-max-20}")
    String citizenId;

    @Size(max = 100, message = "{validation.bank-account-max-100}")
    String bankAccount;

    @Size(max = 50, message = "{validation.health-insurance-number-max-50}")
    String healthInsuranceNumber;

    @Size(max = 500, message = "{validation.identity-card-front-url-max-500}")
    String identityCardFrontUrl;

    @Size(max = 500, message = "{validation.identity-card-back-url-max-500}")
    String identityCardBackUrl;

    Integer identityCardFrontRotate;

    Integer identityCardBackRotate;
}
