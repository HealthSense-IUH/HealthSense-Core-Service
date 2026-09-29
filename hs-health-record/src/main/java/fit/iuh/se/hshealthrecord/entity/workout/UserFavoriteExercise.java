package fit.iuh.se.hshealthrecord.entity.workout;

import fit.iuh.se.hsshared.generator.SnowflakeGenerated;
import fit.iuh.se.hsuser.entity.BaseEntity;
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
@Table(
    name = "user_favorite_exercises",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_exercise_fav", columnNames = {"user_id", "exercise_code"})
    },
    indexes = {
        @Index(name = "idx_fav_exercise_user", columnList = "user_id")
    }
)
public class UserFavoriteExercise extends BaseEntity {

    @Id
    @SnowflakeGenerated
    @Column(name = "id", nullable = false, updatable = false)
    Long id;

    @Column(name = "user_id", nullable = false)
    Long userId;

    @Column(name = "exercise_code", nullable = false, length = 100)
    String exerciseCode;

    @Column(name = "display_order")
    Integer displayOrder;
}
