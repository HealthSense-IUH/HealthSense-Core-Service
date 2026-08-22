package fit.iuh.se.hsuser.entity;

import fit.iuh.se.hsshared.generator.SnowflakeGenerated;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "user_accounts")
public class UserAccount extends BaseEntity {

    @Id
    @SnowflakeGenerated
    @Column(name = "id", nullable = false, updatable = false)
    Long id;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    AccountStatus status;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER, optional = false)
    UserProfile profile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER, optional = false)
    UserSensitiveData sensitiveData;

}
