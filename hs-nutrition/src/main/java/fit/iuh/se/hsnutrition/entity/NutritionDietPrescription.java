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
    /** Bác sĩ dặn riêng các quy tắc thêm ở V29 (quy tắc nền vẫn luôn áp dụng; cờ để nhấn mạnh và chỉnh ngưỡng). */
    @Column(name = "limit_sugars", nullable = false)
    boolean limitSugars;
    @Column(name = "watch_sodium_potassium", nullable = false)
    boolean watchSodiumPotassium;
    @Column(name = "limit_saturated_fat", nullable = false)
    boolean limitSaturatedFat;
    @Column(name = "encourage_magnesium", nullable = false)
    boolean encourageMagnesium;
    @Column(length = 1000)
    String note;

    /**
     * Ngưỡng bác sĩ chỉnh riêng cho hội viên này (V27, V29); null = dùng ngưỡng mặc định ở nutrition_diet_rules.
     * limit = vượt quá là đỏ, caution = vượt quá là vàng, trên 100 g (tỷ lệ Na/K không có đơn vị).
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
    @Column(name = "sugars_limit", precision = 10, scale = 3)
    BigDecimal sugarsLimit;
    @Column(name = "sugars_caution", precision = 10, scale = 3)
    BigDecimal sugarsCaution;
    @Column(name = "saturated_fat_limit", precision = 10, scale = 3)
    BigDecimal saturatedFatLimit;
    @Column(name = "saturated_fat_caution", precision = 10, scale = 3)
    BigDecimal saturatedFatCaution;
    @Column(name = "na_k_ratio_limit", precision = 10, scale = 3)
    BigDecimal naKRatioLimit;
    /** Mức tốt riêng: tỷ lệ Na/K từ mức này trở xuống; magie từ mức này trở lên. */
    @Column(name = "na_k_ratio_good", precision = 10, scale = 3)
    BigDecimal naKRatioGood;
    @Column(name = "magnesium_good", precision = 10, scale = 3)
    BigDecimal magnesiumGood;

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

    /** Bác sĩ có dặn riêng quy tắc này (với VITAMIN_K: đang dùng warfarin, bật thêm quy tắc vitamin K). */
    public boolean isEnabled(DietRuleCode code) {
        return switch (code) {
            case ALCOHOL -> avoidAlcohol;
            case CAFFEINE -> limitCaffeine;
            case SUGARS -> limitSugars;
            case NA_K_RATIO -> watchSodiumPotassium;
            case SODIUM -> limitSodium;
            case SATURATED_FAT -> limitSaturatedFat;
            case MAGNESIUM -> encourageMagnesium;
            case VITAMIN_K -> onWarfarin;
        };
    }

    /** Ngưỡng riêng {limit, caution, good} của một quy tắc; phần tử null = dùng mặc định (hoặc quy tắc không có mức đó). */
    public BigDecimal[] getOverride(DietRuleCode code) {
        return switch (code) {
            case ALCOHOL -> new BigDecimal[]{alcoholLimit, alcoholCaution, null};
            case CAFFEINE -> new BigDecimal[]{caffeineLimit, caffeineCaution, null};
            case SUGARS -> new BigDecimal[]{sugarsLimit, sugarsCaution, null};
            case NA_K_RATIO -> new BigDecimal[]{naKRatioLimit, null, naKRatioGood};
            case SODIUM -> new BigDecimal[]{sodiumLimit, sodiumCaution, null};
            case SATURATED_FAT -> new BigDecimal[]{saturatedFatLimit, saturatedFatCaution, null};
            case MAGNESIUM -> new BigDecimal[]{null, null, magnesiumGood};
            case VITAMIN_K -> new BigDecimal[]{vitaminKLimit, vitaminKCaution, null};
        };
    }

    /** Mức không thuộc quy tắc (ví dụ mức vàng của Na/K) phải để trống; service kiểm tra trước khi gọi. */
    public void setOverride(DietRuleCode code, BigDecimal limit, BigDecimal caution, BigDecimal good) {
        switch (code) {
            case ALCOHOL -> { alcoholLimit = limit; alcoholCaution = caution; }
            case CAFFEINE -> { caffeineLimit = limit; caffeineCaution = caution; }
            case SUGARS -> { sugarsLimit = limit; sugarsCaution = caution; }
            case NA_K_RATIO -> { naKRatioLimit = limit; naKRatioGood = good; }
            case SODIUM -> { sodiumLimit = limit; sodiumCaution = caution; }
            case SATURATED_FAT -> { saturatedFatLimit = limit; saturatedFatCaution = caution; }
            case MAGNESIUM -> magnesiumGood = good;
            case VITAMIN_K -> { vitaminKLimit = limit; vitaminKCaution = caution; }
        }
    }
}
