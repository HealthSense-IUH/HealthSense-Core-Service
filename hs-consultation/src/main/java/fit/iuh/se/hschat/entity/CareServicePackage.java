package fit.iuh.se.hschat.entity;

import fit.iuh.se.hschat.entity.enums.CareServicePackageStatus;
import fit.iuh.se.hsshared.generator.SnowflakeGenerated;
import fit.iuh.se.hsuser.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
        name = "care_service_packages",
        indexes = {
                @Index(name = "idx_care_package_status", columnList = "status")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_care_package_code", columnNames = "code")
        }
)
public class CareServicePackage extends BaseEntity {

    @Id
    @SnowflakeGenerated
    @Column(name = "id", nullable = false, updatable = false)
    Long id;

    @Column(name = "code", nullable = false, length = 80)
    String code;

    @Column(name = "name", nullable = false, length = 160)
    String name;

    @Column(name = "description", length = 1000)
    String description;

    @Column(name = "price_amount", nullable = false, precision = 14, scale = 2)
    BigDecimal priceAmount;

    @Column(name = "currency", nullable = false, length = 3)
    String currency;

    @Column(name = "duration_days", nullable = false)
    Integer durationDays;

    @Column(name = "renewable", nullable = false)
    Boolean renewable;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    CareServicePackageStatus status;
}
