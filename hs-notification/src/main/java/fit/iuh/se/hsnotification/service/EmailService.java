package fit.iuh.se.hsnotification.service;

public interface EmailService {

    void sendForgotPasswordOtpAsync(String email, String otp);
}
