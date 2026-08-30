package fit.iuh.se.hschat.entity;

import fit.iuh.se.hschat.entity.enums.CareServicePackageStatus;
import fit.iuh.se.hschat.entity.enums.CareServiceCode;
import fit.iuh.se.hschat.entity.enums.CareServiceSupportPolicy;
import fit.iuh.se.hschat.entity.enums.DoctorSpecialty;
import fit.iuh.se.hsshared.generator.SnowflakeGenerated;
import fit.iuh.se.hsuser.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
                @Index(name = "idx_care_package_status", columnList = "status"),
                @Index(name = "idx_care_package_family", columnList = "family_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_care_package_family_version",
                        columnNames = {"family_id", "version_number"}
                )
        }
)
public class CareServicePackage extends BaseEntity {

    @Id
    @SnowflakeGenerated
    @Column(name = "id", nullable = false, updatable = false)
    Long id;

    @Column(name = "family_id", nullable = false, updatable = false)
    Long familyId;

    @Column(name = "code", nullable = false, updatable = false, length = 80)
    String code;

    @Column(name = "version_number", nullable = false, updatable = false)
    Integer versionNumber;

    @Column(name = "name", nullable = false, length = 160)
    String name;

    @Column(name = "short_description", length = 500)
    String shortDescription;

    @Column(name = "description", length = 4000)
    String description;

    @Column(name = "price_amount", nullable = false, precision = 14, scale = 2)
    BigDecimal priceAmount;

    @Column(name = "currency", nullable = false, length = 3)
    String currency;

    @Column(name = "duration_days", nullable = false)
    Integer durationDays;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "care_service_package_included_services",
            joinColumns = @JoinColumn(name = "package_version_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "service_code", nullable = false, length = 80)
    @OrderColumn(name = "service_order")
    List<CareServiceCode> includedServices = new ArrayList<>();

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "care_service_package_excluded_services",
            joinColumns = @JoinColumn(name = "package_version_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "service_code", nullable = false, length = 80)
    @OrderColumn(name = "service_order")
    List<CareServiceCode> excludedServices = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "required_specialty", length = 50)
    DoctorSpecialty requiredSpecialty;

    @Enumerated(EnumType.STRING)
    @Column(name = "support_policy", nullable = false, length = 80)
    CareServiceSupportPolicy supportPolicy;

    @Column(name = "renewable", nullable = false)
    Boolean renewable;

    @Column(name = "terms_policy_reference", length = 255)
    String termsPolicyReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    CareServicePackageStatus status;
}
