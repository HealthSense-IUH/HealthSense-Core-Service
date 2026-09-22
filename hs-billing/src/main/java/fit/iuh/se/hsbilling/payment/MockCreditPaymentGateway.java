package fit.iuh.se.hsbilling.payment;

import fit.iuh.se.hsbilling.config.CreditPaymentConfiguration;
import fit.iuh.se.hsbilling.dto.VerifiedCreditPayment;
import fit.iuh.se.hsbilling.entity.enums.CreditPaymentProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MockCreditPaymentGateway {
    private final CreditPaymentConfiguration configuration;

    public VerifiedCreditPayment pay(Long attemptId, long amountVnd, String currency) {
        configuration.requireMockEnabled();
        return new VerifiedCreditPayment(CreditPaymentProvider.MOCK, "mock:" + attemptId, amountVnd, currency);
    }
}
