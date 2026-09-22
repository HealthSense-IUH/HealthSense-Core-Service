package fit.iuh.se.hsbilling.dto;

import fit.iuh.se.hsbilling.entity.CreditPackage;
import fit.iuh.se.hsbilling.entity.enums.CreditPackageStatus;
import java.time.Instant;

public record AdminCreditPackageResponse(String id, String code, String name, String description,
        long creditQuantity, long priceVnd, CreditPackageStatus status, long version,
        Instant createdAt, Instant updatedAt) {
    public static AdminCreditPackageResponse from(CreditPackage value) {
        return new AdminCreditPackageResponse(value.getId().toString(), value.getCode(), value.getName(),
                value.getDescription(), value.getCreditQuantity(), value.getPriceVnd(), value.getStatus(),
                value.getVersion(), value.getCreatedAt(), value.getUpdatedAt());
    }
}
