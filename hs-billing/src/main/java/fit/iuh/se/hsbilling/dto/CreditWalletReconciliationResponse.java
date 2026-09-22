package fit.iuh.se.hsbilling.dto;

public record CreditWalletReconciliationResponse(String memberId, long walletBalance, long walletReserved,
        long ledgerBalance, long ledgerReserved, long heldQuantity, boolean consistent) {}
