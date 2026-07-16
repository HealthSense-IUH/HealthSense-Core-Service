package fit.iuh.se.hsauth.service.token;

import fit.iuh.se.hsauth.entity.RefreshTokenClaims;
import fit.iuh.se.hsuser.entity.UserAccount;

public interface RefreshTokenService {

    String generateRefreshToken(UserAccount user, String sessionId);

    RefreshTokenClaims validateRefreshToken(String refreshToken);
}
