package fit.iuh.se.hsnutrition.repository;

import fit.iuh.se.hsnutrition.entity.NutritionGuidanceFood;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/** Nhóm của món có khuyến nghị là nhóm của thực phẩm nó trỏ tới ({@code food.group}). */
public interface NutritionGuidanceFoodRepository extends JpaRepository<NutritionGuidanceFood, String> {
    @EntityGraph(attributePaths = {"food", "food.group", "evidenceSources"})
    @Query("select g from NutritionGuidanceFood g where g.food.group.id = :groupId order by g.displayOrder")
    List<NutritionGuidanceFood> findByGroupId(@Param("groupId") String groupId);

    @EntityGraph(attributePaths = {"food", "food.group", "evidenceSources"})
    Optional<NutritionGuidanceFood> findWithDetailsById(String id);

    /** {@code pattern} phải đã bỏ dấu và thoát ký tự đặc biệt bằng '!'. */
    @EntityGraph(attributePaths = {"food", "food.group", "evidenceSources"})
    @Query("select g from NutritionGuidanceFood g where g.searchText like :pattern escape '!' order by g.displayOrder")
    List<NutritionGuidanceFood> search(@Param("pattern") String pattern);

    @Query("select g.food.group.id, count(g) from NutritionGuidanceFood g group by g.food.group.id")
    List<Object[]> countByGroup();
}
