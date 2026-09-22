package fit.iuh.se.hsbilling.dto;

import fit.iuh.se.hsbilling.entity.enums.CreditPackageStatus;

public record AdminCreditPackageRequest(String code, String name, String description,
        Long creditQuantity, Long priceVnd, CreditPackageStatus status, Long version) {}
