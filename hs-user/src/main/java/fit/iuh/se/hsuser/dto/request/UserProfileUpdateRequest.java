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

    @Size(max = 120, message = "Display name must not exceed 120 characters")
    String displayName;

    @Size(max = 30, message = "Phone must not exceed 30 characters")
    String phone;

    LocalDate dateOfBirth;

    @Size(max = 20, message = "Gender must not exceed 20 characters")
    String gender;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    String address;

    @Size(max = 50, message = "Timezone must not exceed 50 characters")
    String timezone;

    @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
    String avatarUrl;

    @Size(max = 20, message = "Citizen ID must not exceed 20 characters")
    String citizenId;

    @Size(max = 100, message = "Bank account must not exceed 100 characters")
    String bankAccount;

    @Size(max = 50, message = "Health insurance number must not exceed 50 characters")
    String healthInsuranceNumber;

    String healthData;

    String biometricData;

    @Size(max = 500, message = "Identity card front URL must not exceed 500 characters")
    String identityCardFrontUrl;

    @Size(max = 500, message = "Identity card back URL must not exceed 500 characters")
    String identityCardBackUrl;

    Integer identityCardFrontRotate;

    Integer identityCardBackRotate;
}
