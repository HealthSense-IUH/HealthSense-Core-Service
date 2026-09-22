package fit.iuh.se.hsbilling.dto;

public record CreditPackageResponse(
        String id,
        String code, String name, String description, long creditQuantity, long priceVnd) {}
