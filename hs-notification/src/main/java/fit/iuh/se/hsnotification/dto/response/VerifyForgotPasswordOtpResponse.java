package fit.iuh.se.hsnotification.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class VerifyForgotPasswordOtpResponse {
    String resetToken;
    String tokenType;
    long expiresInSeconds;
}
