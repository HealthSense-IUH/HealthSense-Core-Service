package fit.iuh.se.hsapplication.dto.auth;

import fit.iuh.se.hsuser.entity.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Builder
public class UserAuthentication {
    Long userId;
    String email;
    UserRole role;
}
