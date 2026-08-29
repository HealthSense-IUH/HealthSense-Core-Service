package fit.iuh.se.hsuser.entity;

import fit.iuh.se.hsshared.generator.SnowflakeGenerated;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "user_profiles")
public class UserProfile extends BaseEntity {

    @Id
    @SnowflakeGenerated
    @Column(name = "id", nullable = false, updatable = false)
    Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    UserAccount user;

    @Column(name = "display_name", nullable = false, length = 120)
    String displayName;

    @Column(name = "phone", length = 30)
    String phone;

    @Column(name = "date_of_birth")
    LocalDate dateOfBirth;

    @Column(name = "gender", length = 20)
    String gender;

    @Column(name = "avatar_url", length = 500)
    String avatarUrl;

    @Column(name = "address", length = 500)
    String address;

    @Column(name = "timezone", length = 50)
    String timezone;

    @Column(name = "citizen_id", length = 20)
    String citizenId;

    @Column(name = "bank_account", length = 100)
    String bankAccount;

    @Column(name = "health_insurance_number", length = 50)
    String healthInsuranceNumber;

    @Column(name = "health_data", columnDefinition = "TEXT")
    String healthData;

    @Column(name = "biometric_data", columnDefinition = "TEXT")
    String biometricData;

    @Column(name = "identity_card_front_url", length = 500)
    String identityCardFrontUrl;

    @Column(name = "identity_card_back_url", length = 500)
    String identityCardBackUrl;

    @Builder.Default
    @Column(name = "identity_card_front_rotate")
    Integer identityCardFrontRotate = 0;

    @Builder.Default
    @Column(name = "identity_card_back_rotate")
    Integer identityCardBackRotate = 0;
}
