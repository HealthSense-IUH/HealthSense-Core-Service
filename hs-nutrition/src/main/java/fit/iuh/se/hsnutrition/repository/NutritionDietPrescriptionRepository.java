package fit.iuh.se.hsnutrition.repository;

import fit.iuh.se.hsnutrition.entity.NutritionDietPrescription;
import org.springframework.data.jpa.repository.JpaRepository;

/** Khóa chính là member_id: mỗi hội viên tối đa một đơn ăn uống. */
public interface NutritionDietPrescriptionRepository extends JpaRepository<NutritionDietPrescription, Long> {
}
