package fit.iuh.se.hsbilling.entity;

import fit.iuh.se.hsbilling.entity.enums.*;
import fit.iuh.se.hsshared.generator.SnowflakeGenerated;
import fit.iuh.se.hsuser.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "credit_packages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreditPackage extends BaseEntity {
    @Id @SnowflakeGenerated
    @Column(nullable = false, updatable = false) Long id;
    @Column(nullable = false, unique = true, length = 80) String code;
    @Column(nullable = false, length = 160) String name;
    @Column(length = 1000) String description;
    @Column(name = "credit_quantity", nullable = false) long creditQuantity;
    @Column(name = "price_vnd", nullable = false) long priceVnd;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) CreditPackageStatus status;
    @Version @Column(nullable = false) Long version;
}
