package fit.iuh.se.hsnotification.service.impl;

import fit.iuh.se.hsnotification.dto.command.CreditPurchaseReceiptMailCommand;
import fit.iuh.se.hsnotification.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailServiceImpl implements EmailService {

    private static final DateTimeFormatter RECEIPT_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("HH:mm:ss dd/MM/yyyy")
            .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

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

    @Async
    @Override
    public void sendCreditPurchaseReceiptAsync(CreditPurchaseReceiptMailCommand command) {
        try {
            String safeName = command.displayName() == null || command.displayName().isBlank()
                    ? command.recipientEmail()
                    : command.displayName().trim();
            Context context = new Context(Locale.forLanguageTag("vi-VN"));
            context.setVariable("displayName", safeName);
            context.setVariable("orderId", command.orderId().toString());
            context.setVariable("packageCode", command.packageCode());
            context.setVariable("packageName", command.packageName());
            context.setVariable("creditQuantity", String.format(Locale.US, "%,d", command.creditQuantity()));
            context.setVariable("amount", String.format(Locale.US, "%,d", command.amountVnd()).replace(',', '.'));
            context.setVariable("currency", command.currency());
            context.setVariable("provider", command.provider());
            context.setVariable("paidAt", RECEIPT_TIME_FORMATTER.format(command.paidAt()));
            context.setVariable("walletBalance", String.format(Locale.US, "%,d", command.walletBalance()));
            context.setVariable("walletReserved", String.format(Locale.US, "%,d", command.walletReserved()));
            context.setVariable("walletAvailable", String.format(Locale.US, "%,d", command.walletAvailable()));
            String html = templateEngine.process("mail/credit-purchase-receipt", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );
            helper.setFrom(from);
            helper.setTo(command.recipientEmail());
            helper.setSubject("Thanh toán mua token thành công - HealthSense");
            helper.setText(html, true);

            mailSender.send(message);
        } catch (Exception exception) {
            log.warn("Failed to send credit purchase receipt email for order {} to {}",
                    command.orderId(), command.recipientEmail(), exception);
        }
    }
}
