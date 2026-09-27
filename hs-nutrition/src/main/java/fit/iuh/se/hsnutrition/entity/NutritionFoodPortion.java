package fit.iuh.se.hsnutrition.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

/** Khẩu phần thường dùng của một món, quy ra gram (V21). */
@Entity
@Immutable
@Table(name = "nutrition_food_portions")
@Getter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NutritionFoodPortion {
    @Id Long id;
    @Column(name = "food_id", nullable = false) Long foodId;
    @Column(name = "sequence_number", nullable = false) int sequenceNumber;
    @Column(nullable = false, length = 160) String description;
    @Column(name = "gram_weight", nullable = false) BigDecimal gramWeight;
    @Column(name = "is_default", nullable = false) boolean defaultPortion;
}
