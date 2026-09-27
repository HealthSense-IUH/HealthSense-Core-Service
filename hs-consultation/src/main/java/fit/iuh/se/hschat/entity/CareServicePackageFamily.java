package fit.iuh.se.hschat.entity;

import fit.iuh.se.hsshared.generator.SnowflakeGenerated;
import fit.iuh.se.hsuser.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
        name = "care_service_package_families",
        uniqueConstraints = @UniqueConstraint(name = "uq_care_package_family_code", columnNames = "code")
)
public class CareServicePackageFamily extends BaseEntity {

    @Id
    @SnowflakeGenerated
    @Column(name = "id", nullable = false, updatable = false)
    Long id;

    @Column(name = "code", nullable = false, updatable = false, length = 80)
    String code;
}
