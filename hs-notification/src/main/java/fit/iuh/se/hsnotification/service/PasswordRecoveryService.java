package fit.iuh.se.hsnotification.service;

import fit.iuh.se.hsnotification.dto.request.ForgotPasswordOtpRequest;
import fit.iuh.se.hsnotification.dto.request.ResetPasswordRequest;
import fit.iuh.se.hsnotification.dto.request.VerifyForgotPasswordOtpRequest;
import fit.iuh.se.hsnotification.dto.response.ForgotPasswordOtpResponse;
import fit.iuh.se.hsnotification.dto.response.VerifyForgotPasswordOtpResponse;

public interface PasswordRecoveryService {

    ForgotPasswordOtpResponse requestOtp(ForgotPasswordOtpRequest request);

    VerifyForgotPasswordOtpResponse verifyOtp(VerifyForgotPasswordOtpRequest request);

    void resetPassword(ResetPasswordRequest request);
}
