package fit.iuh.se.hsbilling.payment;

import fit.iuh.se.hsbilling.dto.CreditPaymentLink;
import java.time.Instant;

public interface CreditPaymentGateway {
    CreditPaymentLink createPaymentLink(Long orderCode, long amountVnd, String description,
            String returnUrl, String cancelUrl, Instant expiresAt);
    String getPaymentStatus(Long orderCode);
    void cancelPaymentLink(Long orderCode, String reason);
}
