package fit.iuh.se.hsauth.service.token;

import fit.iuh.se.hsuser.entity.UserAccount;

public interface AccessTokenService {

    String generateAccessToken(UserAccount user);
}
