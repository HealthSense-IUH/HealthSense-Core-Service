package fit.iuh.se.hsuser.dto.response;

import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {

    Long id;
    String email;
    UserRole role;
    AccountStatus status;
    String displayName;
    String phone;
    LocalDate dateOfBirth;
    String gender;
    String avatarUrl;
    String address;
    String timezone;
    String citizenId;
    String bankAccount;
    String healthInsuranceNumber;
    String healthData;
    String biometricData;
    String identityCardFrontUrl;
    String identityCardBackUrl;
    Integer identityCardFrontRotate;
    Integer identityCardBackRotate;
    Instant createdAt;
    Instant updatedAt;
}
