package fit.iuh.se.hsnotification.service;

public interface RedisOtpService {

    String createForgotPasswordOtp(String email);

    boolean verifyForgotPasswordOtp(String email, String otp);
}
