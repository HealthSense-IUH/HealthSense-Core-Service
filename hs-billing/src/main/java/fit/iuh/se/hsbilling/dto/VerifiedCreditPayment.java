package fit.iuh.se.hsbilling.dto;

import fit.iuh.se.hsbilling.entity.enums.CreditPaymentProvider;

// Internal gateway evidence; never deserialize this DTO from a public request.
public record VerifiedCreditPayment(CreditPaymentProvider provider, String reference, String paymentLinkId,
        long amountVnd, String currency) {
    public VerifiedCreditPayment(CreditPaymentProvider provider, String reference, long amountVnd, String currency) {
        this(provider, reference, reference, amountVnd, currency);
    }
}
