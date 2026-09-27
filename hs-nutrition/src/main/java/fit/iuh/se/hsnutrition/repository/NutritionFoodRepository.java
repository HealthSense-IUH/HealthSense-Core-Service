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
     * Chữ hoa có dấu -> chữ thường, để so khớp có dấu mà không phụ thuộc locale của database
     * (lower() của PostgreSQL chỉ đổi chữ ASCII khi collation là "C").
     */
    String VI_UPPER = "ÀÁẢÃẠĂẰẮẲẴẶÂẦẤẨẪẬÈÉẺẼẸÊỀẾỂỄỆÌÍỈĨỊÒÓỎÕỌÔỒỐỔỖỘƠỜỚỞỠỢÙÚỦŨỤƯỪỨỬỮỰỲÝỶỸỴĐ";
    String VI_LOWER = "àáảãạăằắẳẵặâầấẩẫậèéẻẽẹêềếểễệìíỉĩịòóỏõọôồốổỗộơờớởỡợùúủũụưừứửữựỳýỷỹỵđ";
    String NAME_VI_LOWERCASE = "lower(translate(coalesce(f.name_vi, ''), '" + VI_UPPER + "', '" + VI_LOWER + "'))";

    String SEARCH_FILTER = """
            where (cast(:groupId as text) is null or f.group_id = cast(:groupId as text))
              and (cast(:source as text) is null or f.source = cast(:source as text))
              and (to_tsvector('english', f.name) @@ to_tsquery('english', cast(:tsquery as text))
                   or to_tsvector('simple', coalesce(f.search_vi, '')) @@ to_tsquery('simple', cast(:tsquery as text)))
            """;

    /**
     * Tìm đồng thời theo tên gốc (full-text 'english', index của V21) và tên tiếng Việt đã bỏ dấu
     * (full-text 'simple' trên cột search_vi, index của V23). Biểu thức phải giữ đúng như index để dùng được.
     * Vì so khớp bỏ dấu ("pho" khớp cả "phở" lẫn "phô mai"), món chứa đúng cụm người dùng gõ, kể cả dấu
     * ({@code phrase}, dạng LIKE đã thoát bằng '!'), xếp trước; sau đó theo độ khớp, rồi theo tên hiển thị
     * (tên Việt cho nguồn VN_FCT, tên gốc cho USDA, khớp displayName ở service).
     * {@code tsquery} do service dựng từ các token đã lọc, không nhận chuỗi người dùng trực tiếp.
     */
    @Query(value = "select f.* from nutrition_foods f " + SEARCH_FILTER
            + "order by case when " + NAME_VI_LOWERCASE + " like cast(:phrase as text) escape '!'"
            + " or lower(f.name) like cast(:phrase as text) escape '!' then 0 else 1 end," + """
                     greatest(
                         ts_rank(to_tsvector('english', f.name), to_tsquery('english', cast(:tsquery as text))),
                         ts_rank(to_tsvector('simple', coalesce(f.search_vi, '')), to_tsquery('simple', cast(:tsquery as text)))) desc,
                     case when f.source = 'VN_FCT' then coalesce(f.name_vi, f.name) else f.name end, f.id
            """,
            countQuery = "select count(*) from nutrition_foods f " + SEARCH_FILTER,
            nativeQuery = true)
    Page<NutritionFood> search(@Param("tsquery") String tsquery, @Param("phrase") String phrase,
                               @Param("groupId") String groupId, @Param("source") String source, Pageable pageable);

    @Query(value = """
            select f.* from nutrition_foods f
            where (cast(:groupId as text) is null or f.group_id = cast(:groupId as text))
              and (cast(:source as text) is null or f.source = cast(:source as text))
            order by case when f.source = 'VN_FCT' then coalesce(f.name_vi, f.name) else f.name end, f.id
            """,
            countQuery = """
            select count(*) from nutrition_foods f
            where (cast(:groupId as text) is null or f.group_id = cast(:groupId as text))
              and (cast(:source as text) is null or f.source = cast(:source as text))
            """,
            nativeQuery = true)
    Page<NutritionFood> browse(@Param("groupId") String groupId, @Param("source") String source, Pageable pageable);

    /** Mỗi dòng: group id, source, số thực phẩm. Nguồn Việt Nam đứng trước trong từng nhóm. */
    @Query("""
            select f.group.id, f.source, count(f) from NutritionFood f
            group by f.group.id, f.source
            order by f.group.id, case when f.source = 'VN_FCT' then 0 else 1 end
            """)
    List<Object[]> countByGroupAndSource();
}
