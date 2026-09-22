package fit.iuh.se.hsapplication.payment;

import fit.iuh.se.hsbilling.dto.CreditPaymentLink;
import fit.iuh.se.hsbilling.payment.CreditPaymentGateway;
import fit.iuh.se.hschat.service.payment.PayOSPaymentGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class PayOSCreditPaymentGatewayAdapter implements CreditPaymentGateway {
    private final PayOSPaymentGateway payOS;

    @Override
    public CreditPaymentLink createPaymentLink(Long orderCode, long amountVnd, String description,
            String returnUrl, String cancelUrl, Instant expiresAt) {
        var link = payOS.createPaymentLink(orderCode, amountVnd, description, returnUrl, cancelUrl, expiresAt);
        return new CreditPaymentLink(link.getOrderCode(), link.getPaymentLinkId(), link.getCheckoutUrl(), expiresAt);
    }

    @Override public String getPaymentStatus(Long orderCode) { return payOS.getPaymentStatus(orderCode); }
    @Override public void cancelPaymentLink(Long orderCode, String reason) { payOS.cancelPaymentLink(orderCode, reason); }
}
