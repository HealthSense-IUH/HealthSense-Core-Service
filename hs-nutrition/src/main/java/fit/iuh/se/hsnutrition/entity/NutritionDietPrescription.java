package fit.iuh.se.hsnutrition.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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

import fit.iuh.se.hsnutrition.entity.enums.DietRuleCode;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Đơn ăn uống bác sĩ kê cho một hội viên (V26). Mỗi hội viên một dòng, lần kê sau ghi đè lần trước.
 * Các cờ quyết định cách chấm màu thực phẩm cho hội viên đó (xem DietAdvisor).
 */
@Entity
@Table(name = "nutrition_diet_prescriptions")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NutritionDietPrescription {
    @Id
    @Column(name = "member_id")
    Long memberId;

    @Column(name = "limit_sodium", nullable = false)
    boolean limitSodium;
    /** Đang dùng warfarin: cần giữ lượng vitamin K ổn định mỗi ngày. */
    @Column(name = "on_warfarin", nullable = false)
    boolean onWarfarin;
    @Column(name = "avoid_alcohol", nullable = false)
    boolean avoidAlcohol;
    @Column(name = "limit_caffeine", nullable = false)
    boolean limitCaffeine;
    @Column(length = 1000)
    String note;

    /**
     * Ngưỡng bác sĩ chỉnh riêng cho hội viên này (V27); null = dùng ngưỡng mặc định ở nutrition_diet_rules.
     * limit = từ mức này là đỏ, caution = từ mức này là vàng, trên 100 g.
     */
    @Column(name = "sodium_limit", precision = 10, scale = 3)
    BigDecimal sodiumLimit;
    @Column(name = "sodium_caution", precision = 10, scale = 3)
    BigDecimal sodiumCaution;
    @Column(name = "alcohol_limit", precision = 10, scale = 3)
    BigDecimal alcoholLimit;
    @Column(name = "alcohol_caution", precision = 10, scale = 3)
    BigDecimal alcoholCaution;
    @Column(name = "caffeine_limit", precision = 10, scale = 3)
    BigDecimal caffeineLimit;
    @Column(name = "caffeine_caution", precision = 10, scale = 3)
    BigDecimal caffeineCaution;
    @Column(name = "vitamin_k_limit", precision = 10, scale = 3)
    BigDecimal vitaminKLimit;
    @Column(name = "vitamin_k_caution", precision = 10, scale = 3)
    BigDecimal vitaminKCaution;

    /** Bác sĩ kê lần gần nhất và phiên tư vấn khi kê. */
    @Column(name = "prescribed_by", nullable = false)
    Long prescribedBy;
    @Column(name = "consultation_session_id")
    Long consultationSessionId;

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

    public boolean isEnabled(DietRuleCode code) {
        return switch (code) {
            case SODIUM -> limitSodium;
            case ALCOHOL -> avoidAlcohol;
            case CAFFEINE -> limitCaffeine;
            case VITAMIN_K -> onWarfarin;
            default -> false;
        };
    }

    /** Ngưỡng riêng {limit, caution} của một quy tắc; phần tử null = dùng mặc định. */
    public BigDecimal[] getOverride(DietRuleCode code) {
        return switch (code) {
            case SODIUM -> new BigDecimal[]{sodiumLimit, sodiumCaution};
            case ALCOHOL -> new BigDecimal[]{alcoholLimit, alcoholCaution};
            case CAFFEINE -> new BigDecimal[]{caffeineLimit, caffeineCaution};
            case VITAMIN_K -> new BigDecimal[]{vitaminKLimit, vitaminKCaution};
            default -> new BigDecimal[]{null, null};
        };
    }

    public void setOverride(DietRuleCode code, BigDecimal limit, BigDecimal caution) {
        switch (code) {
            case SODIUM -> { sodiumLimit = limit; sodiumCaution = caution; }
            case ALCOHOL -> { alcoholLimit = limit; alcoholCaution = caution; }
            case CAFFEINE -> { caffeineLimit = limit; caffeineCaution = caution; }
            case VITAMIN_K -> { vitaminKLimit = limit; vitaminKCaution = caution; }
            default -> {
                if (limit != null || caution != null)
                    throw new IllegalArgumentException(code + " has no per-member threshold");
            }
        }
    }
}
