package fit.iuh.se.hsnutrition.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Immutable;

/**
 * Nhóm thực phẩm chung cho mọi nguồn (V24): mỗi dòng {@link NutritionFood} thuộc đúng một nhóm, món có khuyến nghị
 * lấy nhóm theo thực phẩm nó trỏ tới. Nhóm chỉ là phân loại; mức khuyến nghị nằm ở từng món.
 */
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
    /** Tên icon lucide-react mà Frontend hiển thị. */
    @Column(length = 40) String icon;
    @Column(name = "image_url", length = 500) String imageUrl;
    @Column(name = "display_order", nullable = false) int displayOrder;
}
