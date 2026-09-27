package fit.iuh.se.hsnutrition.repository;

import fit.iuh.se.hsnutrition.entity.NutritionFoodGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NutritionFoodGroupRepository extends JpaRepository<NutritionFoodGroup, String> {
    List<NutritionFoodGroup> findAllByOrderByDisplayOrderAsc();

    Optional<NutritionFoodGroup> findBySlug(String slug);
}
