package fit.iuh.se.hsbilling.entity;

import fit.iuh.se.hsbilling.entity.enums.*;
import fit.iuh.se.hsshared.generator.SnowflakeGenerated;
import fit.iuh.se.hsuser.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "credit_wallets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreditWallet extends BaseEntity {
    @Id @SnowflakeGenerated
    @Column(nullable = false, updatable = false) Long id;
    @Column(name = "member_id", nullable = false, unique = true) Long memberId;
    @Column(nullable = false) long balance;
    @Column(nullable = false) long reserved;
    @Version @Column(nullable = false) Long version;
}
