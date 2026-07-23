package fit.iuh.se.hsnotification.service.impl;

import fit.iuh.se.hsauth.dto.token.PasswordResetTokenClaims;
import fit.iuh.se.hsauth.service.token.PasswordResetTokenService;
import fit.iuh.se.hsnotification.dto.request.ForgotPasswordOtpRequest;
import fit.iuh.se.hsnotification.dto.request.ResetPasswordRequest;
import fit.iuh.se.hsnotification.dto.request.VerifyForgotPasswordOtpRequest;
import fit.iuh.se.hsnotification.dto.response.ForgotPasswordOtpResponse;
import fit.iuh.se.hsnotification.dto.response.VerifyForgotPasswordOtpResponse;
import fit.iuh.se.hsnotification.service.EmailService;
import fit.iuh.se.hsnotification.service.RedisOtpService;
import fit.iuh.se.hsnotification.service.PasswordRecoveryService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.utils.TextNormalize;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PasswordRecoveryServiceImpl implements PasswordRecoveryService {

    UserAccountRepository userAccountRepository;
    RedisOtpService redisOtpService;
    EmailService emailService;
    PasswordResetTokenService passwordResetTokenService;
    PasswordEncoder passwordEncoder;

    @NonFinal
    @Value("${app.notification.reset-token.ttl}")
    Duration resetTokenTtl;

    @Override
    public ForgotPasswordOtpResponse requestOtp(ForgotPasswordOtpRequest request) {
        String email = TextNormalize.normalizeEmail(request.getEmail());
        userAccountRepository.findUserByEmail(email)
                .filter(user -> user.getStatus() == AccountStatus.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String otp = redisOtpService.createForgotPasswordOtp(email);
        emailService.sendForgotPasswordOtpAsync(email, otp);

        return new ForgotPasswordOtpResponse("OTP has been sent");
    }

    @Override
    public VerifyForgotPasswordOtpResponse verifyOtp(VerifyForgotPasswordOtpRequest request) {
        String email = TextNormalize.normalizeEmail(request.getEmail());
        if (!redisOtpService.verifyForgotPasswordOtp(email, request.getOtp()))
            throw new AppException(ErrorCode.INVALID_TOKEN, "OTP is invalid or expired");

        UserAccount user = userAccountRepository.findUserByEmail(email)
                .filter(account -> account.getStatus() == AccountStatus.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));

        String resetToken = passwordResetTokenService.generatePasswordResetToken(user);
        return new VerifyForgotPasswordOtpResponse(resetToken, "Bearer", resetTokenTtl.toSeconds());
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetTokenClaims claims = passwordResetTokenService.validatePasswordResetToken(request.getResetToken());
        UserAccount user = userAccountRepository.findById(claims.getUserId())
                .filter(account -> account.getStatus() == AccountStatus.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userAccountRepository.save(user);
    }
}
