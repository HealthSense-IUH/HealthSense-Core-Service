package fit.iuh.se.hsnotification.service.impl;

import fit.iuh.se.hsnotification.service.RedisOtpService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedisOtpServiceImpl implements RedisOtpService {

    private static final String FORGOT_PASSWORD_OTP_PREFIX = "notification:forgot-password:otp:";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    StringRedisTemplate redisTemplate;

    @NonFinal
    @Value("${app.notification.otp.ttl}")
    Duration otpTtl;

    @Override
    public String createForgotPasswordOtp(String email) {
        String otp = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
        redisTemplate.opsForValue().set(key(email), otp, otpTtl);
        return otp;
    }

    @Override
    public boolean verifyForgotPasswordOtp(String email, String otp) {
        String redisKey = key(email);
        String storedOtp = redisTemplate.opsForValue().get(redisKey);
        if (!otp.equals(storedOtp)) return false;

        redisTemplate.delete(redisKey);
        return true;
    }

    private String key(String email) {
        return FORGOT_PASSWORD_OTP_PREFIX + email;
    }
}
