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
     * Tìm đồng thời theo tên tiếng Anh (full-text 'english', index của V21) và tên tiếng Việt đã bỏ dấu
     * (full-text 'simple' trên cột search_vi, index của V23). Biểu thức phải giữ đúng như index để dùng được.
     * {@code tsquery} do service dựng từ các token đã lọc, không nhận chuỗi người dùng trực tiếp.
     */
    @Query(value = """
            select f.* from nutrition_foods f
            where (cast(:category as text) is null or f.category = cast(:category as text))
              and (cast(:source as text) is null or f.source = cast(:source as text))
              and (to_tsvector('english', f.name) @@ to_tsquery('english', cast(:tsquery as text))
                   or to_tsvector('simple', coalesce(f.search_vi, '')) @@ to_tsquery('simple', cast(:tsquery as text)))
            order by greatest(
                         ts_rank(to_tsvector('english', f.name), to_tsquery('english', cast(:tsquery as text))),
                         ts_rank(to_tsvector('simple', coalesce(f.search_vi, '')), to_tsquery('simple', cast(:tsquery as text)))) desc,
                     coalesce(f.name_vi, f.name), f.id
            """,
            countQuery = """
            select count(*) from nutrition_foods f
            where (cast(:category as text) is null or f.category = cast(:category as text))
              and (cast(:source as text) is null or f.source = cast(:source as text))
              and (to_tsvector('english', f.name) @@ to_tsquery('english', cast(:tsquery as text))
                   or to_tsvector('simple', coalesce(f.search_vi, '')) @@ to_tsquery('simple', cast(:tsquery as text)))
            """,
            nativeQuery = true)
    Page<NutritionFood> search(@Param("tsquery") String tsquery, @Param("category") String category,
                               @Param("source") String source, Pageable pageable);

    @Query(value = """
            select f.* from nutrition_foods f
            where (cast(:category as text) is null or f.category = cast(:category as text))
              and (cast(:source as text) is null or f.source = cast(:source as text))
            order by coalesce(f.name_vi, f.name), f.id
            """,
            countQuery = """
            select count(*) from nutrition_foods f
            where (cast(:category as text) is null or f.category = cast(:category as text))
              and (cast(:source as text) is null or f.source = cast(:source as text))
            """,
            nativeQuery = true)
    Page<NutritionFood> browse(@Param("category") String category, @Param("source") String source, Pageable pageable);

    /** Nhóm của nguồn Việt Nam đứng trước, rồi đến nhóm USDA; mỗi nguồn xếp theo tên nhóm. */
    @Query("""
            select f.source, f.category, count(f) from NutritionFood f where f.category is not null
            group by f.source, f.category
            order by case when f.source = 'VN_FCT' then 0 else 1 end, f.category
            """)
    List<Object[]> countByCategory();
}
