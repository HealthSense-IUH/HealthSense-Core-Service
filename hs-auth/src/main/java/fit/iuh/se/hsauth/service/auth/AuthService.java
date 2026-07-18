package fit.iuh.se.hsauth.service.auth;

import fit.iuh.se.hsauth.dto.request.LoginRequest;
import fit.iuh.se.hsauth.dto.request.RegisterRequest;
import fit.iuh.se.hsauth.dto.response.LoginResponse;
import fit.iuh.se.hsauth.dto.response.RegisterResponse;
import fit.iuh.se.hsauth.dto.token.AuthenticationResult;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    AuthenticationResult login(LoginRequest request);

    LoginResponse mobileLogin(LoginRequest request);

    AuthenticationResult refresh(String refreshToken, String sessionId);

    void logout(String refreshToken, String sessionId);
}
