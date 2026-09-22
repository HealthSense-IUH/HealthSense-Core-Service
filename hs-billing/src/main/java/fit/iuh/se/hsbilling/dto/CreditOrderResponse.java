package fit.iuh.se.hsbilling.dto;

public record CreditOrderResponse(CreditOrderSummary order, CreditPaymentSummary payment, CreditWalletResponse wallet) {}
