package fit.iuh.se.hsnutrition.entity;

import fit.iuh.se.hsnutrition.entity.enums.DietRuleCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Ngưỡng mặc định của một quy tắc chấm màu (V27, V28), admin sửa được. Trên 100 g phần ăn được, so "vượt quá mức này":
 * > limitThreshold là đỏ, > cautionThreshold là vàng; goodThreshold là mức tốt cho nhịp tim (tỷ lệ Na/K: từ mức này
 * trở xuống; magie: từ mức này trở lên). null = quy tắc không có mức đó. evidence / evidenceUrl: nguồn của quy tắc.
 */
@Entity
@Table(name = "nutrition_diet_rules")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NutritionDietRule {
    @Id
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    DietRuleCode code;

    @Column(nullable = false, length = 120)
    String name;
    @Column(nullable = false, length = 10)
    String unit;
    @Column(name = "limit_threshold", precision = 10, scale = 3)
    BigDecimal limitThreshold;
    @Column(name = "caution_threshold", precision = 10, scale = 3)
    BigDecimal cautionThreshold;

    @Column(name = "good_threshold", precision = 10, scale = 3)
    BigDecimal goodThreshold;

    @Column(columnDefinition = "TEXT")
    String evidence;
    // Bản tiếng Anh (V30), trả khi request tiếng Anh; NULL thì dùng bản tiếng Việt
    @Column(name = "name_en", length = 120)
    String nameEn;
    @Column(name = "evidence_en", columnDefinition = "TEXT")
    String evidenceEn;

    @Column(name = "evidence_url", length = 500)
    String evidenceUrl;
    @Column(name = "display_order", nullable = false)
    int displayOrder;

    @Version
    Long version;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    Instant createdAt;
    @LastModifiedDate
    @Column(name = "updated_at")
    Instant updatedAt;
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    String createdBy;
    @LastModifiedBy
    @Column(name = "updated_by")
    String updatedBy;
}
