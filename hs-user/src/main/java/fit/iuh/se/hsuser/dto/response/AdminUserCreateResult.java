package fit.iuh.se.hsuser.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminUserCreateResult {

    UserResponse user;
    String temporaryPassword;
}
