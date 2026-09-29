package fit.iuh.se.hsnutrition.repository;

import fit.iuh.se.hsnutrition.entity.NutritionDietRule;
import fit.iuh.se.hsnutrition.entity.enums.DietRuleCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NutritionDietRuleRepository extends JpaRepository<NutritionDietRule, DietRuleCode> {
    List<NutritionDietRule> findAllByOrderByDisplayOrderAsc();
}
