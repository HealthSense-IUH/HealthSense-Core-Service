package fit.iuh.se.hsnotification.service.impl;

import fit.iuh.se.hsnotification.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailServiceImpl implements EmailService {

    JavaMailSender mailSender;
    SpringTemplateEngine templateEngine;

    @NonFinal
    @Value("${app.notification.mail.from}")
    String from;

    @NonFinal
    @Value("${app.notification.otp.ttl}")
    Duration otpTtl;

    @Async
    @Override
    public void sendForgotPasswordOtpAsync(String email, String otp) {
        try {
            Context context = new Context();
            context.setVariable("otp", otp);
            context.setVariable("validMinutes", otpTtl.toMinutes());
            String html = templateEngine.process("mail/forgot-password-otp", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );
            helper.setFrom(from);
            helper.setTo(email);
            helper.setSubject("HealthSense password reset OTP");
            helper.setText(html, true);

            mailSender.send(message);
        } catch (Exception exception) {
            log.warn("Failed to send forgot-password OTP email to {}", email, exception);
        }
    }

    @Async
    @Override
    public void sendTemporaryPasswordAsync(String email, String displayName, String role, String temporaryPassword) {
        try {
            String safeName = displayName == null || displayName.isBlank() ? email : displayName.trim();
            String html = """
                    <div style="font-family:Arial,sans-serif;line-height:1.5;color:#1f2937">
                      <h2>Your HealthSense account has been created</h2>
                      <p>Hello %s,</p>
                      <p>Your %s account has been created. Please use the temporary password below to log in.</p>
                      <p style="font-size:20px;font-weight:700;letter-spacing:2px;background:#f3f4f6;padding:12px 16px;border-radius:6px;display:inline-block">%s</p>
                      <p>Please change your password after your first login.</p>
                    </div>
                    """.formatted(safeName, role, temporaryPassword);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );
            helper.setFrom(from);
            helper.setTo(email);
            helper.setSubject("Your HealthSense account");
            helper.setText(html, true);

            mailSender.send(message);
        } catch (Exception exception) {
            log.warn("Failed to send temporary password email to {}", email, exception);
        }
    }
}
