package fit.iuh.se.hsbilling.dto;

// Only verified purchase orders may fund the wallet through this phase-1 contract.
// Administrative adjustments/refunds will have separate authorized operations.
public record CreditSource(Long purchaseOrderId) {}
