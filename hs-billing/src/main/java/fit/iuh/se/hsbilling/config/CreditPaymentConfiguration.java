package fit.iuh.se.hsbilling.config;

import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.time.Duration;
import fit.iuh.se.hsbilling.entity.enums.CreditPaymentProvider;

@Component
public class CreditPaymentConfiguration {
    private final boolean mockEnabled;
    private final CreditPaymentProvider provider;
    private final String returnUrl;
    private final String cancelUrl;
    private final Duration linkTtl;

    public CreditPaymentConfiguration(Environment environment) {
        String provider = environment.getProperty("app.billing.payment-provider", "DISABLED").trim().toUpperCase(Locale.ROOT);
        boolean enabled = environment.getProperty("app.billing.mock-payment-enabled", Boolean.class, false);
        Set<String> profiles = new java.util.HashSet<>(Arrays.asList(environment.getActiveProfiles()));
        boolean testEnvironment = profiles.contains("test");
        boolean production = profiles.contains("prod") || profiles.contains("production");
        if (!Set.of("DISABLED", "MOCK", "PAYOS").contains(provider))
            throw new IllegalStateException("Billing provider must be DISABLED, MOCK or PAYOS");
        if (enabled && (!testEnvironment || production))
            throw new IllegalStateException("Mock credit payments may only be enabled by automated tests");
        if (provider.equals("MOCK") && (!enabled || !testEnvironment || production))
            throw new IllegalStateException("MOCK billing requires an explicitly enabled test configuration");
        if (enabled && !provider.equals("MOCK"))
            throw new IllegalStateException("mock-payment-enabled requires payment-provider=MOCK");
        this.mockEnabled = provider.equals("MOCK") && enabled;
        this.provider = provider.equals("PAYOS") ? CreditPaymentProvider.PAYOS
                : provider.equals("MOCK") ? CreditPaymentProvider.MOCK : null;
        this.returnUrl = environment.getProperty("app.billing.payos.return-url", "");
        this.cancelUrl = environment.getProperty("app.billing.payos.cancel-url", "");
        this.linkTtl = environment.getProperty("app.billing.payos.link-ttl", Duration.class, Duration.ofMinutes(15));
        if (this.provider == CreditPaymentProvider.PAYOS
                && (returnUrl.isBlank() || cancelUrl.isBlank() || linkTtl.isZero() || linkTtl.isNegative()))
            throw new IllegalStateException("PAYOS billing requires return URL, cancel URL and a positive link TTL");
    }

    public void requireMockEnabled() {
        if (!mockEnabled) throw new AppException(ErrorCode.CREDIT_PURCHASE_DISABLED);
    }

    public CreditPaymentProvider requireEnabledProvider() {
        if (provider == null) throw new AppException(ErrorCode.CREDIT_PURCHASE_DISABLED);
        return provider;
    }

    public void requireProvider(CreditPaymentProvider expected) {
        if (expected == CreditPaymentProvider.MOCK) requireMockEnabled();
        else if (expected != CreditPaymentProvider.PAYOS || provider != CreditPaymentProvider.PAYOS)
            throw new AppException(ErrorCode.CREDIT_PURCHASE_DISABLED);
    }

    public String returnUrl() { return returnUrl; }
    public String cancelUrl() { return cancelUrl; }
    public Duration linkTtl() { return linkTtl; }
}
