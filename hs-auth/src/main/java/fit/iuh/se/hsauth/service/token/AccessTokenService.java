package fit.iuh.se.hsauth.service.token;

import fit.iuh.se.hsuser.entity.UserAccount;

import java.time.Duration;

public interface AccessTokenService {

    String generateAccessToken(UserAccount user);

    String generateAccessToken(UserAccount user, Duration tokenTtl);
}
