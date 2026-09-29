package fit.iuh.se.hsauth.dto.request;

import jakarta.validation.constraints.NotBlank;
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
public class MobileLogoutRequest {

    @NotBlank(message = "{validation.refresh-token-required}")
    String refreshToken;

    @NotBlank(message = "{validation.session-id-required}")
    String sessionId;
}
