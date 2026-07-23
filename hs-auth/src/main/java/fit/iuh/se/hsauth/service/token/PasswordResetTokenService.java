package fit.iuh.se.hsauth.service.token;

import fit.iuh.se.hsauth.dto.token.PasswordResetTokenClaims;
import fit.iuh.se.hsuser.entity.UserAccount;

public interface PasswordResetTokenService {

    String generatePasswordResetToken(UserAccount user);

    PasswordResetTokenClaims validatePasswordResetToken(String resetToken);
}
