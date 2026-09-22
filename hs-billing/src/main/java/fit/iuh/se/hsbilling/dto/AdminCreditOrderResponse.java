package fit.iuh.se.hsbilling.dto;

import java.util.List;

public record AdminCreditOrderResponse(String memberId, CreditOrderSummary order,
        List<CreditPaymentSummary> attempts) {}
