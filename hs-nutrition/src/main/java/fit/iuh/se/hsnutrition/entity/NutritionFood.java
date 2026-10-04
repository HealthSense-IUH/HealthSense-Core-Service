package fit.iuh.se.hsnutrition.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

/**
 * Thành phần dinh dưỡng trên 100 g phần ăn được. Dữ liệu tham chiếu, chỉ đọc, gồm hai nguồn:
 * USDA FNDDS (V21, source = USDA_FNDDS) và Bảng thành phần thực phẩm Việt Nam 2007 (V23, source = VN_FCT).
 */
@Entity
@Immutable
@Table(name = "nutrition_foods")
@Getter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NutritionFood {
    @Id
    Long id;
    @Column(nullable = false, length = 30)
    String source;
    @Column(name = "source_version", nullable = false, length = 30)
    String sourceVersion;
    @Column(name = "source_food_code", nullable = false, length = 20)
    String sourceFoodCode;
    /**
     * Tên gốc của nguồn (USDA: tiếng Anh).
     */
    @Column(nullable = false)
    String name;
    /**
     * Tên tiếng Việt: tên trong sách với VN_FCT, tên dịch (V25) với USDA.
     */
    @Column(name = "name_vi")
    String nameVi;
    /**
     * Phân loại gốc của nguồn (USDA: nhóm WWEIA; Việt Nam: nhóm của sách). Nhóm chung nằm ở {@link #group}.
     */
    @Column(length = 160)
    String category;
    /** Tên phân loại tiếng Anh cho nguồn Việt Nam (V30); USDA để NULL vì category đã là tiếng Anh. */
    @Column(name = "category_en", length = 160)
    String categoryEn;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    NutritionFoodGroup group;
    /**
     * Tỉ lệ thải bỏ khi sơ chế (%), chỉ nguồn VN_FCT có.
     */
    @Column(name = "waste_pct")
    BigDecimal wastePct;

    @Column(name = "energy_kcal")
    BigDecimal energyKcal;
    @Column(name = "protein_g")
    BigDecimal proteinG;
    @Column(name = "carbohydrate_g")
    BigDecimal carbohydrateG;
    @Column(name = "fiber_g")
    BigDecimal fiberG;
    /**
     * Xơ thô (celluloza) của nguồn VN_FCT; khác phương pháp với xơ tiêu hóa fiber_g của USDA.
     */
    @Column(name = "fiber_crude_g")
    BigDecimal fiberCrudeG;
    @Column(name = "sugars_g")
    BigDecimal sugarsG;
    @Column(name = "fat_total_g")
    BigDecimal fatTotalG;
    @Column(name = "fat_saturated_g")
    BigDecimal fatSaturatedG;
    @Column(name = "fat_monounsaturated_g")
    BigDecimal fatMonounsaturatedG;
    @Column(name = "fat_polyunsaturated_g")
    BigDecimal fatPolyunsaturatedG;
    @Column(name = "cholesterol_mg")
    BigDecimal cholesterolMg;
    @Column(name = "sodium_mg")
    BigDecimal sodiumMg;
    @Column(name = "potassium_mg")
    BigDecimal potassiumMg;
    @Column(name = "magnesium_mg")
    BigDecimal magnesiumMg;
    @Column(name = "caffeine_mg")
    BigDecimal caffeineMg;
    @Column(name = "alcohol_g")
    BigDecimal alcoholG;
    @Column(name = "vitamin_k_mcg")
    BigDecimal vitaminKMcg;
    @Column(name = "epa_g")
    BigDecimal epaG;
    @Column(name = "dha_g")
    BigDecimal dhaG;
}
