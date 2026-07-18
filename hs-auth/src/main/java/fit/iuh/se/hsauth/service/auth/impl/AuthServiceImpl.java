package fit.iuh.se.hsauth.service.auth.impl;

import fit.iuh.se.hsauth.dto.request.LoginRequest;
import fit.iuh.se.hsauth.dto.request.RegisterRequest;
import fit.iuh.se.hsauth.dto.response.LoginResponse;
import fit.iuh.se.hsauth.dto.response.RegisterResponse;
import fit.iuh.se.hsauth.dto.session.AuthSession;
import fit.iuh.se.hsauth.dto.token.RefreshTokenClaims;
import fit.iuh.se.hsauth.mapper.AuthUserMapper;
import fit.iuh.se.hsauth.dto.token.AuthenticationResult;
import fit.iuh.se.hsauth.service.auth.AuthService;
import fit.iuh.se.hsauth.service.session.AuthSessionService;
import fit.iuh.se.hsauth.service.token.AccessTokenService;
import fit.iuh.se.hsauth.service.token.RefreshTokenService;
import fit.iuh.se.hsauth.service.token.TokenHashService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.UserProfile;
import fit.iuh.se.hsuser.entity.UserSensitiveData;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsuser.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthServiceImpl implements AuthService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    AuthUserMapper authUserMapper;
    AccessTokenService accessTokenService;
    RefreshTokenService refreshTokenService;
    TokenHashService tokenHashService;
    AuthSessionService authSessionService;

    @NonFinal
    @Value("${security.jwt.refresh-token-ttl}")
    Duration refreshTokenTtl;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmail(email))
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);

        UserAccount user = UserAccount.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.MEMBER)
                .status(AccountStatus.ACTIVE)
                .build();

        UserProfile profile = UserProfile.builder()
                .user(user)
                .displayName(request.getFullName().trim())
                .build();

        UserSensitiveData sensitiveData = UserSensitiveData.builder()
                .user(user)
                .build();

        user.setProfile(profile);
        user.setSensitiveData(sensitiveData);

        return authUserMapper.toUserSession(userRepository.save(user));
    }

    @Override
    public AuthenticationResult login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());
        UserAccount userAccount = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        validateActiveAccount(userAccount);
        if (!passwordEncoder.matches(request.getPassword(), userAccount.getPasswordHash()))
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);

        return createAuthenticationResult(userAccount, UUID.randomUUID().toString());
    }

    @Override
    public AuthenticationResult refresh(String refreshToken, String sessionId) {
        validateRefreshRequest(refreshToken, sessionId);

        RefreshTokenClaims claims = refreshTokenService.validateRefreshToken(refreshToken);
        if (!Objects.equals(claims.getSessionId(), sessionId))
            throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);

        AuthSession session = authSessionService.findById(claims.getUserId(), sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_FOUND));

        String refreshTokenHash = tokenHashService.hash(refreshToken);
        if (!Objects.equals(session.getRefreshTokenHash(), refreshTokenHash)) {
            authSessionService.revoke(claims.getUserId(), sessionId);
            throw new AppException(ErrorCode.REFRESH_TOKEN_REUSED);
        }

        UserAccount userAccount = userRepository.findById(claims.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        validateActiveAccount(userAccount);

        String accessToken = accessTokenService.generateAccessToken(userAccount);
        String newRefreshToken = refreshTokenService.generateRefreshToken(userAccount, sessionId);
        authSessionService.updateRefreshTokenHash(
                userAccount.getId(),
                sessionId,
                tokenHashService.hash(newRefreshToken),
                refreshTokenTtl
        );

        return new AuthenticationResult(
                buildLoginResponse(accessToken),
                newRefreshToken,
                sessionId
        );
    }

    @Override
    public void logout(String refreshToken, String sessionId) {
        validateRefreshRequest(refreshToken, sessionId);

        RefreshTokenClaims claims = refreshTokenService.validateRefreshToken(refreshToken);
        if (!Objects.equals(claims.getSessionId(), sessionId))
            throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);

        authSessionService.revoke(claims.getUserId(), sessionId);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private AuthenticationResult createAuthenticationResult(UserAccount userAccount, String sessionId) {
        Instant createdAt = Instant.now();
        Instant expiresAt = createdAt.plus(refreshTokenTtl);
        String accessToken = accessTokenService.generateAccessToken(userAccount);
        String refreshToken = refreshTokenService.generateRefreshToken(userAccount, sessionId);

        authSessionService.save(AuthSession.builder()
                .userId(userAccount.getId())
                .sessionId(sessionId)
                .refreshTokenHash(tokenHashService.hash(refreshToken))
                .createdAt(createdAt)
                .expiresAt(expiresAt)
                .build(), refreshTokenTtl);

        return new AuthenticationResult(
                buildLoginResponse(accessToken),
                refreshToken,
                sessionId
        );
    }

    private LoginResponse buildLoginResponse(String accessToken) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .build();
    }

    private void validateActiveAccount(UserAccount userAccount) {
        if (userAccount.getActive() == null || !userAccount.getActive())
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
    }

    private void validateRefreshRequest(String refreshToken, String sessionId) {
        if (refreshToken == null || refreshToken.isBlank() || sessionId == null || sessionId.isBlank())
            throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
    }
}
