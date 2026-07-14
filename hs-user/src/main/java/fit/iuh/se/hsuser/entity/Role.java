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
@Table(name = "roles")
public class Role extends fit.iuh.se.hsuser.entity.BaseEntity {

    @Id
    @SnowflakeGenerated
    @Column(name = "id", nullable = false, updatable = false)
    Long id;

    @Column(name = "name", nullable = false, length = 100)
    String name;

    @Column(name = "description", nullable = false, length = 100)
    String description;
}
