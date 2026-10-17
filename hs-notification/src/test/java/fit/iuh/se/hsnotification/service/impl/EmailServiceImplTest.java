package fit.iuh.se.hsnotification.service.impl;

import fit.iuh.se.hsnotification.dto.command.CreditPurchaseReceiptMailCommand;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailServiceImplTest {

    @Test
    void creditPurchaseReceiptTemplateIsRenderedAndSent() throws Exception {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(message);

        var resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        var templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(resolver);

        var service = new EmailServiceImpl(mailSender, templateEngine);
        ReflectionTestUtils.setField(service, "from", "no-reply@healthsense.test");
        service.sendCreditPurchaseReceiptAsync(new CreditPurchaseReceiptMailCommand(
                "member@example.com", "Nguyễn Văn An", 99L, "TOKEN_10", "Gói 10 token",
                10, 100_000, "VND", "PAYOS", Instant.parse("2026-10-16T17:30:00Z"),
                14, 2, 12
        ));

        verify(mailSender).send(message);
        assertEquals("Thanh toán mua token thành công - HealthSense", message.getSubject());
        assertEquals("member@example.com", message.getAllRecipients()[0].toString());
    }
}
