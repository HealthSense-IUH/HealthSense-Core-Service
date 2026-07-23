package fit.iuh.se.hsnotification.service;

public interface EmailService {

    void sendForgotPasswordOtpAsync(String email, String otp);

    void sendTemporaryPasswordAsync(String email, String displayName, String role, String temporaryPassword);
}
