package fit.iuh.se.hsnutrition.service;

import fit.iuh.se.hsnutrition.dto.NutritionDietPrescriptionResponse;
import fit.iuh.se.hsnutrition.dto.UpdateDietPrescriptionRequest;

/**
 * Đơn ăn uống theo hội viên. Không kiểm tra quyền: nơi gọi (controller / service ở hs-application) phải bảo đảm
 * hội viên chỉ đọc đơn của mình và bác sĩ chỉ kê cho hội viên trong phiên tư vấn của mình.
 */
public interface DietPrescriptionService {
    /** Chưa có đơn thì trả lời khuyên chung với {@code personalized = false}. */
    NutritionDietPrescriptionResponse get(Long memberId);

    NutritionDietPrescriptionResponse save(Long memberId, Long doctorId, Long consultationSessionId,
                                           UpdateDietPrescriptionRequest request);

    /** Các cờ để chấm màu thực phẩm; chưa có đơn thì {@link DietProfile#GENERAL}. */
    DietProfile profileOf(Long memberId);
}
