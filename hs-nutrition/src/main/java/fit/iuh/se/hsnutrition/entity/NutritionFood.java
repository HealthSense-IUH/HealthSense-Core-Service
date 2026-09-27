package fit.iuh.se.hsnutrition.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

/** Thành phần dinh dưỡng trên 100 g (V21, nguồn USDA FNDDS). Dữ liệu tham chiếu, chỉ đọc. */
@Entity
@Immutable
@Table(name = "nutrition_foods")
@Getter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NutritionFood {
    @Id Long id;
    @Column(nullable = false, length = 30) String source;
    @Column(name = "source_version", nullable = false, length = 30) String sourceVersion;
    @Column(name = "source_food_code", nullable = false, length = 20) String sourceFoodCode;
    @Column(nullable = false) String name;
    @Column(name = "name_vi") String nameVi;
    @Column(length = 160) String category;

    @Column(name = "energy_kcal") BigDecimal energyKcal;
    @Column(name = "protein_g") BigDecimal proteinG;
    @Column(name = "carbohydrate_g") BigDecimal carbohydrateG;
    @Column(name = "fiber_g") BigDecimal fiberG;
    @Column(name = "sugars_g") BigDecimal sugarsG;
    @Column(name = "fat_total_g") BigDecimal fatTotalG;
    @Column(name = "fat_saturated_g") BigDecimal fatSaturatedG;
    @Column(name = "fat_monounsaturated_g") BigDecimal fatMonounsaturatedG;
    @Column(name = "fat_polyunsaturated_g") BigDecimal fatPolyunsaturatedG;
    @Column(name = "cholesterol_mg") BigDecimal cholesterolMg;
    @Column(name = "sodium_mg") BigDecimal sodiumMg;
    @Column(name = "potassium_mg") BigDecimal potassiumMg;
    @Column(name = "magnesium_mg") BigDecimal magnesiumMg;
    @Column(name = "caffeine_mg") BigDecimal caffeineMg;
    @Column(name = "alcohol_g") BigDecimal alcoholG;
    @Column(name = "vitamin_k_mcg") BigDecimal vitaminKMcg;
    @Column(name = "epa_g") BigDecimal epaG;
    @Column(name = "dha_g") BigDecimal dhaG;
}
