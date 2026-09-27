package fit.iuh.se.hsnutrition.repository;

import fit.iuh.se.hsnutrition.entity.NutritionGuidanceFood;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NutritionGuidanceFoodRepository extends JpaRepository<NutritionGuidanceFood, String> {
    @EntityGraph(attributePaths = {"group", "food", "evidenceSources"})
    List<NutritionGuidanceFood> findByGroupIdOrderByDisplayOrderAsc(String groupId);

    @EntityGraph(attributePaths = {"group", "food", "evidenceSources"})
    Optional<NutritionGuidanceFood> findWithDetailsById(String id);

    /** {@code pattern} phải đã bỏ dấu và thoát ký tự đặc biệt bằng '!'. */
    @EntityGraph(attributePaths = {"group", "food", "evidenceSources"})
    @Query("select g from NutritionGuidanceFood g where g.searchText like :pattern escape '!' order by g.displayOrder")
    List<NutritionGuidanceFood> search(@Param("pattern") String pattern);

    @Query("select g.group.id, count(g) from NutritionGuidanceFood g group by g.group.id")
    List<Object[]> countByGroup();

    long countByGroupId(String groupId);
}
