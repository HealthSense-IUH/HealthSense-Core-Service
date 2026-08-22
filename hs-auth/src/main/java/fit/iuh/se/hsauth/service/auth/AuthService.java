package fit.iuh.se.hsauth.service.auth;

import fit.iuh.se.hsauth.dto.request.LoginRequest;
import fit.iuh.se.hsauth.dto.request.MobileLogoutRequest;
import fit.iuh.se.hsauth.dto.request.MobileRefreshRequest;
import fit.iuh.se.hsauth.dto.request.RegisterRequest;
import fit.iuh.se.hsauth.dto.response.LoginResponse;
import fit.iuh.se.hsauth.dto.response.MobileLoginResponse;
import fit.iuh.se.hsauth.dto.response.RegisterResponse;
import fit.iuh.se.hsauth.dto.token.AuthenticationResult;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    AuthenticationResult login(LoginRequest request);

    MobileLoginResponse mobileLogin(LoginRequest request);

    MobileLoginResponse mobileRefresh(MobileRefreshRequest request);

    AuthenticationResult refresh(String refreshToken, String sessionId);

    void logout(String refreshToken, String sessionId);

    void mobileLogout(MobileLogoutRequest request);
}

