package fit.iuh.se.hsuser.entity;

import fit.iuh.se.hsshared.generator.SnowflakeGenerated;
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
@Table(name = "user_sensitive_data")
public class UserSensitiveData extends BaseEntity {

    @Id
    @SnowflakeGenerated
    @Column(name = "id", nullable = false, updatable = false)
    Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    UserAccount user;

    @Column(name = "citizen_id", length = 20)
    String citizenId;

    @Column(name = "bank_account", length = 100)
    String bankAccount;

    @Column(name = "health_insurance_number", length = 50)
    String healthInsuranceNumber;

    @Lob
    @Column(name = "health_data")
    String healthData;

    @Lob
    @Column(name = "biometric_data")
    String biometricData;
}
