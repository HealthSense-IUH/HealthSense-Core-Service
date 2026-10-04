package fit.iuh.se.hshealthrecord.entity.workout;

import fit.iuh.se.hsshared.generator.SnowflakeGenerated;
import fit.iuh.se.hsuser.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(
    name = "user_daily_steps",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_step_date", columnNames = {"user_id", "step_date"})
    },
    indexes = {
        @Index(name = "idx_user_daily_steps_user_date", columnList = "user_id, step_date")
    }
)
public class UserDailyStep extends BaseEntity {

    @Id
    @SnowflakeGenerated
    @Column(name = "id", nullable = false, updatable = false)
    Long id;

    @Column(name = "user_id", nullable = false)
    Long userId;

    @Column(name = "step_date", nullable = false)
    LocalDate stepDate;

    @Column(name = "total_steps", nullable = false)
    Integer totalSteps;

    @Column(name = "distance_meters")
    Double distanceMeters;

    @Column(name = "calories_burned")
    Integer caloriesBurned;

    @Column(name = "active_minutes")
    Integer activeMinutes;

    @Column(name = "target_steps", nullable = false)
    Integer targetSteps;

    @Column(name = "hourly_breakdown_json", columnDefinition = "TEXT")
    String hourlyBreakdownJson;

    @Column(name = "device_source", length = 50)
    String deviceSource;

    @Column(name = "synced_at")
    Instant syncedAt;
}
