package fit.iuh.se.hsapplication.service.nutrition;

import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hsnutrition.dto.NutritionDietPrescriptionResponse;
import fit.iuh.se.hsnutrition.dto.UpdateDietPrescriptionRequest;
import fit.iuh.se.hsnutrition.service.DietPrescriptionService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Bác sĩ xem và kê đơn ăn uống cho hội viên của một phiên tư vấn. Quyền theo cùng quy tắc với hồ sơ sức khỏe
 * trong phiên (DoctorActiveCareServiceImpl): bác sĩ phải là bác sĩ của phiên, phiên đã kích hoạt; xem được cả khi
 * phiên đã kết thúc, nhưng chỉ kê/sửa khi phiên đang ACTIVE và tài khoản bác sĩ đang hoạt động.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DoctorDietPrescriptionService {
    static final Set<ConsultationStatus> READABLE = Set.of(
            ConsultationStatus.ACTIVE, ConsultationStatus.COMPLETED, ConsultationStatus.CANCELLED);

    ConsultationSessionRepository sessions;
    UserAccountRepository accounts;
    DietPrescriptionService prescriptions;

    @Transactional(readOnly = true)
    public NutritionDietPrescriptionResponse get(Long doctorId, Long sessionId) {
        return prescriptions.get(readableSession(doctorId, sessionId).getMemberId());
    }

    @Transactional
    public NutritionDietPrescriptionResponse update(Long doctorId, Long sessionId, UpdateDietPrescriptionRequest request) {
        if (request == null) throw new AppException(ErrorCode.INVALID_REQUEST_BODY);
        ConsultationSession session = readableSession(doctorId, sessionId);
        if (session.getStatus() != ConsultationStatus.ACTIVE)
            throw AppException.of(ErrorCode.CONSULTATION_NOT_ACTIVE, "detail.diet-prescription-requires-active");
        if (accounts.findById(doctorId).filter(account -> account.getStatus() == AccountStatus.ACTIVE).isEmpty())
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        return prescriptions.save(session.getMemberId(), doctorId, session.getId(), request);
    }

    private ConsultationSession readableSession(Long doctorId, Long sessionId) {
        ConsultationSession session = sessions.findByIdAndDoctorId(sessionId, doctorId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED));
        if (session.getActivatedAt() == null || !READABLE.contains(session.getStatus()))
            throw new AppException(ErrorCode.CONSULTATION_NOT_ACTIVE);
        return session;
    }
}
