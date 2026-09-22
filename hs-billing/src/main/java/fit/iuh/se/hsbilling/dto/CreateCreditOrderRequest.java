package fit.iuh.se.hsbilling.dto;

// Only package selection is accepted; member, price, credits and payment result are server-owned.
public record CreateCreditOrderRequest(Long packageId) {}
