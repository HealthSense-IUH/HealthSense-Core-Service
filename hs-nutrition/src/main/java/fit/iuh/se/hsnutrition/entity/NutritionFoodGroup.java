package fit.iuh.se.hsnutrition.entity;

import fit.iuh.se.hsnutrition.entity.enums.DietaryPattern;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "nutrition_food_groups")
@Getter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NutritionFoodGroup {
    @Id @Column(length = 40) String id;
    @Column(nullable = false, unique = true, length = 60) String slug;
    @Column(nullable = false, length = 120) String name;
    @Column(length = 500) String description;
    @Enumerated(EnumType.STRING) @Column(name = "dietary_pattern", nullable = false, length = 20) DietaryPattern dietaryPattern;
    @Column(length = 40) String icon;
    @Column(name = "image_url", length = 500) String imageUrl;
    @Column(name = "display_order", nullable = false) int displayOrder;
}
