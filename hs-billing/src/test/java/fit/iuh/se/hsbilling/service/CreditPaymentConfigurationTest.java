package fit.iuh.se.hsbilling.service;

import fit.iuh.se.hsbilling.config.CreditPaymentConfiguration;
import fit.iuh.se.hsbilling.payment.MockCreditPaymentGateway;
import fit.iuh.se.hsbilling.dto.VerifiedCreditPayment;
import fit.iuh.se.hsbilling.entity.enums.CreditPaymentProvider;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mock.env.MockEnvironment;
import static org.junit.jupiter.api.Assertions.*;

class CreditPaymentConfigurationTest {
    @Test void defaultConfigurationDisablesPaymentsAndGateway() {
        var config = new CreditPaymentConfiguration(new MockEnvironment());
        assertEquals(ErrorCode.CREDIT_PURCHASE_DISABLED,
                assertThrows(AppException.class, config::requireMockEnabled).getErrorCode());
        assertThrows(AppException.class, () -> new MockCreditPaymentGateway(config).pay(1L, 1000, "VND"));
    }

    @Test void explicitlyEnabledTestCanPay() {
        var env = enabled(); env.setActiveProfiles("test");
        var gateway = new MockCreditPaymentGateway(new CreditPaymentConfiguration(env));
        assertEquals(new VerifiedCreditPayment(CreditPaymentProvider.MOCK, "mock:7", 1000, "VND"),
                gateway.pay(7L, 1000, "VND"));
    }

    @Test void productionEvenAlongsideDevRejectsStartup() {
        for (String[] profiles : new String[][]{{"dev"}, {"prod"}, {"production"}, {"dev", "prod"}, {"test", "production"}, {"billing-mock"}}) {
            var env = enabled(); env.setActiveProfiles(profiles);
            try (var context = new AnnotationConfigApplicationContext()) {
                context.setEnvironment(env);
                context.register(CreditPaymentConfiguration.class);
                assertThrows(org.springframework.beans.BeansException.class, context::refresh);
            }
        }
    }

    @Test void missingFlagAndUnsupportedProviderFailClosed() {
        var env = new MockEnvironment().withProperty("app.billing.payment-provider", "MOCK");
        env.setActiveProfiles("dev");
        assertThrows(IllegalStateException.class, () -> new CreditPaymentConfiguration(env));
        for (String provider : new String[]{"PAYOS", "typo", "DISABLED"}) {
            var invalid = enabled().withProperty("app.billing.payment-provider", provider);
            invalid.setActiveProfiles("dev");
            assertThrows(IllegalStateException.class, () -> new CreditPaymentConfiguration(invalid));
        }
    }

    @Test void payOSRequiresHostedCheckoutUrlsAndDoesNotRequireMockFlag() {
        var env = new MockEnvironment()
                .withProperty("app.billing.payment-provider", "PAYOS")
                .withProperty("app.billing.payos.return-url", "https://app.example/credits/payment/result")
                .withProperty("app.billing.payos.cancel-url", "https://app.example/credits/payment/cancel");
        var config = new CreditPaymentConfiguration(env);
        assertEquals(CreditPaymentProvider.PAYOS, config.requireEnabledProvider());
        assertThrows(AppException.class, config::requireMockEnabled);

        var missingUrls = new MockEnvironment().withProperty("app.billing.payment-provider", "PAYOS");
        assertThrows(IllegalStateException.class, () -> new CreditPaymentConfiguration(missingUrls));
    }

    private MockEnvironment enabled() {
        return new MockEnvironment().withProperty("app.billing.payment-provider", "MOCK")
                .withProperty("app.billing.mock-payment-enabled", "true");
    }
}
