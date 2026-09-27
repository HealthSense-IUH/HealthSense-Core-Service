package fit.iuh.se.hsnutrition.repository;

import fit.iuh.se.hsnutrition.entity.NutritionFood;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NutritionFoodRepository extends JpaRepository<NutritionFood, Long> {
    /**
     * Full-text theo tên, dùng đúng biểu thức của index idx_nutrition_food_name_fts (V21).
     * {@code tsquery} phải do service dựng từ các token đã lọc, không nhận chuỗi người dùng trực tiếp.
     */
    @Query(value = """
            select f.* from nutrition_foods f
            where (cast(:category as text) is null or f.category = cast(:category as text))
              and to_tsvector('english', f.name) @@ to_tsquery('english', cast(:tsquery as text))
            order by ts_rank(to_tsvector('english', f.name), to_tsquery('english', cast(:tsquery as text))) desc,
                     f.name, f.id
            """,
            countQuery = """
            select count(*) from nutrition_foods f
            where (cast(:category as text) is null or f.category = cast(:category as text))
              and to_tsvector('english', f.name) @@ to_tsquery('english', cast(:tsquery as text))
            """,
            nativeQuery = true)
    Page<NutritionFood> search(@Param("tsquery") String tsquery, @Param("category") String category, Pageable pageable);

    @Query(value = """
            select f.* from nutrition_foods f
            where (cast(:category as text) is null or f.category = cast(:category as text))
            order by f.name, f.id
            """,
            countQuery = """
            select count(*) from nutrition_foods f
            where (cast(:category as text) is null or f.category = cast(:category as text))
            """,
            nativeQuery = true)
    Page<NutritionFood> browse(@Param("category") String category, Pageable pageable);

    @Query("select f.category, count(f) from NutritionFood f where f.category is not null group by f.category order by f.category")
    List<Object[]> countByCategory();
}
