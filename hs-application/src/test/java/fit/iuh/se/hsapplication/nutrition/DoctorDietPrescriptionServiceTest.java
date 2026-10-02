package fit.iuh.se.hsapplication.nutrition;

import fit.iuh.se.hsapplication.service.nutrition.DoctorDietPrescriptionService;
import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hsnutrition.dto.NutritionDietPrescriptionResponse;
import fit.iuh.se.hsnutrition.dto.UpdateDietPrescriptionRequest;
import fit.iuh.se.hsnutrition.service.DietPrescriptionService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/** Quyền kê đơn ăn uống theo phiên tư vấn: cùng quy tắc với hồ sơ sức khỏe trong phiên. */
class DoctorDietPrescriptionServiceTest {
    static final long DOCTOR = 77L;
    static final long MEMBER = 123L;
    static final long SESSION = 555L;
    static final UpdateDietPrescriptionRequest REQUEST = new UpdateDietPrescriptionRequest(true, true, true, false, null, null);
    static final NutritionDietPrescriptionResponse RESPONSE = new NutritionDietPrescriptionResponse(
            MEMBER, true, true, true, true, false, false, false, false, false, null, DOCTOR, SESSION, null, List.of());

    ConsultationSessionRepository sessions;
    UserAccountRepository accounts;
    DietPrescriptionService prescriptions;
    DoctorDietPrescriptionService service;

    @BeforeEach
    void setUp() {
        sessions = mock(ConsultationSessionRepository.class);
        accounts = mock(UserAccountRepository.class);
        prescriptions = mock(DietPrescriptionService.class);
        service = new DoctorDietPrescriptionService(sessions, accounts, prescriptions);
        when(accounts.findById(DOCTOR)).thenReturn(Optional.of(UserAccount.builder().status(AccountStatus.ACTIVE).build()));
        when(prescriptions.get(MEMBER)).thenReturn(RESPONSE);
        when(prescriptions.save(MEMBER, DOCTOR, SESSION, REQUEST)).thenReturn(RESPONSE);
    }

    private void session(ConsultationStatus status, Instant activatedAt) {
        when(sessions.findByIdAndDoctorId(SESSION, DOCTOR)).thenReturn(Optional.of(ConsultationSession.builder()
                .id(SESSION).memberId(MEMBER).doctorId(DOCTOR).status(status).activatedAt(activatedAt).build()));
    }

    private void error(ErrorCode code, Runnable action) {
        assertEquals(code, assertThrows(AppException.class, action::run).getErrorCode());
    }

    @Test
    void scheduledOrActiveSessionDoctorReadsAndPrescribesForThatSessionsMember() {
        session(ConsultationStatus.ACTIVE, Instant.now());
        assertSame(RESPONSE, service.get(DOCTOR, SESSION));
        assertSame(RESPONSE, service.update(DOCTOR, SESSION, REQUEST));
        // Đã lên lịch, chưa kích hoạt: bác sĩ vẫn xem và kê đơn trước được
        session(ConsultationStatus.SCHEDULED, null);
        assertSame(RESPONSE, service.get(DOCTOR, SESSION));
        assertSame(RESPONSE, service.update(DOCTOR, SESSION, REQUEST));
        verify(prescriptions, times(2)).save(MEMBER, DOCTOR, SESSION, REQUEST);
    }

    @Test
    void finishedSessionIsReadOnly() {
        for (ConsultationStatus status : new ConsultationStatus[]{ConsultationStatus.COMPLETED, ConsultationStatus.CANCELLED}) {
            session(status, Instant.now());
            assertSame(RESPONSE, service.get(DOCTOR, SESSION));
            error(ErrorCode.CONSULTATION_NOT_ACTIVE, () -> service.update(DOCTOR, SESSION, REQUEST));
        }
        verify(prescriptions, never()).save(anyLong(), anyLong(), anyLong(), any());
    }

    @Test
    void otherDoctorsSessionsAreDenied() {
        when(sessions.findByIdAndDoctorId(SESSION, DOCTOR)).thenReturn(Optional.empty());
        error(ErrorCode.CONSULTATION_ACCESS_DENIED, () -> service.get(DOCTOR, SESSION));
        error(ErrorCode.CONSULTATION_ACCESS_DENIED, () -> service.update(DOCTOR, SESSION, REQUEST));
        verify(prescriptions, never()).get(anyLong());
    }

    @Test
    void disabledDoctorCannotPrescribe() {
        session(ConsultationStatus.ACTIVE, Instant.now());
        when(accounts.findById(DOCTOR)).thenReturn(Optional.of(UserAccount.builder().status(AccountStatus.INACTIVE).build()));
        error(ErrorCode.ACCOUNT_DISABLED, () -> service.update(DOCTOR, SESSION, REQUEST));
        error(ErrorCode.INVALID_REQUEST_BODY, () -> service.update(DOCTOR, SESSION, null));
    }
}
