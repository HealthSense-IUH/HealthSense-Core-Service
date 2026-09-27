package fit.iuh.se.hsnutrition.repository;

import fit.iuh.se.hsnutrition.entity.NutritionFoodPortion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NutritionFoodPortionRepository extends JpaRepository<NutritionFoodPortion, Long> {
    List<NutritionFoodPortion> findByFoodIdOrderBySequenceNumberAsc(Long foodId);
}
