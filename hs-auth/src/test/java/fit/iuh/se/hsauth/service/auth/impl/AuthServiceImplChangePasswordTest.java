package fit.iuh.se.hsauth.service.auth.impl;

import fit.iuh.se.hsauth.dto.request.ChangePasswordRequest;
import fit.iuh.se.hsauth.mapper.AuthUserMapper;
import fit.iuh.se.hsauth.service.session.AuthSessionService;
import fit.iuh.se.hsauth.service.token.AccessTokenService;
import fit.iuh.se.hsauth.service.token.RefreshTokenService;
import fit.iuh.se.hsauth.service.token.TokenHashService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplChangePasswordTest {

    @Mock UserAccountRepository userAccountRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthUserMapper authUserMapper;
    @Mock AccessTokenService accessTokenService;
    @Mock RefreshTokenService refreshTokenService;
    @Mock TokenHashService tokenHashService;
    @Mock AuthSessionService authSessionService;

    @InjectMocks AuthServiceImpl service;

    @Test
    void changesPasswordAndRevokesEverySession() {
        UserAccount user = activeUser("old-hash");
        ChangePasswordRequest request = request("OldPassword123", "NewPassword456");
        when(userAccountRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPassword123", "old-hash")).thenReturn(true);
        when(passwordEncoder.matches("NewPassword456", "old-hash")).thenReturn(false);
        when(passwordEncoder.encode("NewPassword456")).thenReturn("new-hash");

        service.changePassword(10L, request);

        assertEquals("new-hash", user.getPasswordHash());
        verify(userAccountRepository).save(user);
        verify(authSessionService).revokeAll(10L);
    }

    @Test
    void rejectsIncorrectCurrentPasswordWithoutChangingAnything() {
        UserAccount user = activeUser("old-hash");
        when(userAccountRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword123", "old-hash")).thenReturn(false);

        AppException exception = assertThrows(AppException.class,
                () -> service.changePassword(10L, request("WrongPassword123", "NewPassword456")));

        assertEquals(ErrorCode.CURRENT_PASSWORD_INCORRECT, exception.getErrorCode());
        verify(userAccountRepository, never()).save(user);
        verifyNoInteractions(authSessionService);
    }

    @Test
    void rejectsNewPasswordWhenItMatchesCurrentPassword() {
        UserAccount user = activeUser("old-hash");
        when(userAccountRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("SamePassword123", "old-hash")).thenReturn(true);

        AppException exception = assertThrows(AppException.class,
                () -> service.changePassword(10L, request("SamePassword123", "SamePassword123")));

        assertEquals(ErrorCode.NEW_PASSWORD_SAME_AS_CURRENT, exception.getErrorCode());
        verify(userAccountRepository, never()).save(user);
        verifyNoInteractions(authSessionService);
    }

    @Test
    void rejectsDisabledAccount() {
        UserAccount user = activeUser("old-hash");
        user.setStatus(AccountStatus.INACTIVE);
        when(userAccountRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(user));

        AppException exception = assertThrows(AppException.class,
                () -> service.changePassword(10L, request("OldPassword123", "NewPassword456")));

        assertEquals(ErrorCode.ACCOUNT_DISABLED, exception.getErrorCode());
        verifyNoInteractions(passwordEncoder, authSessionService);
        verify(userAccountRepository, never()).save(user);
    }

    private UserAccount activeUser(String passwordHash) {
        return UserAccount.builder()
                .id(10L)
                .passwordHash(passwordHash)
                .status(AccountStatus.ACTIVE)
                .build();
    }

    private ChangePasswordRequest request(String currentPassword, String newPassword) {
        return ChangePasswordRequest.builder()
                .currentPassword(currentPassword)
                .newPassword(newPassword)
                .build();
    }
}
