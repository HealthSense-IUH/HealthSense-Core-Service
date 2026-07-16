package fit.iuh.se.hsauth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * @author : user664dntp
 * @mailto : phatdang19052004@gmail.com
 * @created : 16/07/2026, Thursday
 **/

@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Builder
public class LoginResponse {
    String accessToken;
    String tokenType;
}
