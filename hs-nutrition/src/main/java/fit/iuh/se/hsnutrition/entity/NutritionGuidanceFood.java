package fit.iuh.se.hsnutrition.entity;

import fit.iuh.se.hsnutrition.entity.enums.GuidanceType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Immutable;

import java.util.ArrayList;
import java.util.List;

/**
 * Món trong danh mục có khuyến nghị (V22). Chỉ giữ nội dung tư vấn; số liệu dinh dưỡng
 * luôn đọc từ {@link NutritionFood} qua nutrition_food_id để không có hai nguồn lệch nhau.
 */
@Entity
@Immutable
@Table(name = "nutrition_guidance_foods")
@Getter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NutritionGuidanceFood {
    @Id @Column(length = 60) String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    NutritionFoodGroup group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nutrition_food_id", nullable = false)
    NutritionFood food;

    @Column(name = "food_name", nullable = false, length = 120) String foodName;
    @Column(name = "food_name_specific", nullable = false, length = 160) String foodNameSpecific;
    @Column(length = 1000) String description;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) GuidanceType guidance;
    @Column(name = "guidance_title", nullable = false, length = 200) String guidanceTitle;
    @Column(name = "guidance_reason", nullable = false, length = 1000) String guidanceReason;
    @Column(name = "cardiovascular_context", length = 1000) String cardiovascularContext;
    @Column(name = "af_context", length = 1000) String afContext;
    @Column(name = "medication_context", length = 1000) String medicationContext;
    /** Mã chất dinh dưỡng nổi bật, cách nhau bằng dấu phẩy, theo thứ tự hiển thị. */
    @Column(name = "highlight_nutrient_codes", nullable = false, length = 200) String highlightNutrientCodes;
    @Column(name = "image_url", length = 500) String imageUrl;
    /** Chữ thường, bỏ dấu tiếng Việt. Món thêm bằng migration sau phải điền cột này. */
    @Column(name = "search_text", nullable = false, length = 2000) String searchText;
    @Column(name = "display_order", nullable = false) int displayOrder;

    @ManyToMany
    @JoinTable(name = "nutrition_guidance_food_evidence",
            joinColumns = @JoinColumn(name = "guidance_food_id"),
            inverseJoinColumns = @JoinColumn(name = "evidence_source_id"))
    @OrderColumn(name = "display_order")
    List<NutritionEvidenceSource> evidenceSources = new ArrayList<>();
}
