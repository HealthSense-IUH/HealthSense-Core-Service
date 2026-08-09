package fit.iuh.se.hschat.service.doctor.impl;

import fit.iuh.se.hschat.dto.request.DoctorCareProfileRequest;
import fit.iuh.se.hschat.dto.response.DoctorCareProfileResponse;
import fit.iuh.se.hschat.entity.DoctorCareProfile;
import fit.iuh.se.hschat.mapper.ConsultationMapper;
import fit.iuh.se.hschat.repository.DoctorCareProfileRepository;
import fit.iuh.se.hschat.service.doctor.DoctorCareProfileService;
import fit.iuh.se.hschat.service.doctor.SupportScheduleValidator;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DoctorCareProfileServiceImpl implements DoctorCareProfileService {

    static final String DEFAULT_TIMEZONE = "Asia/Ho_Chi_Minh";

    DoctorCareProfileRepository profileRepository;
    UserAccountRepository userAccountRepository;
    SupportScheduleValidator scheduleValidator;
    ConsultationMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public DoctorCareProfileResponse getProfile(Long actorId, UserRole actorRole, Long doctorId) {
        validateConsultationManager(actorRole);
        return profileRepository.findByDoctorId(doctorId)
                .map(mapper::toDoctorCareProfileResponse)
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_CARE_PROFILE_NOT_FOUND));
    }

    @Override
    @Transactional
    public DoctorCareProfileResponse upsertProfile(
            Long actorId,
            UserRole actorRole,
            Long doctorId,
            DoctorCareProfileRequest request
    ) {
        validateConsultationManager(actorRole);
        validateDoctor(doctorId);

        String timezone = isBlank(request.getTimezone()) ? DEFAULT_TIMEZONE : request.getTimezone().trim();
        boolean acceptingCare = Boolean.TRUE.equals(request.getAcceptsOneOnOneCare());
        scheduleValidator.validate(request.getAvailabilityJson(), timezone, acceptingCare);

        DoctorCareProfile profile = profileRepository.findByDoctorId(doctorId)
                .orElseGet(() -> DoctorCareProfile.builder()
                        .doctorId(doctorId)
                        .build());

        profile.setSpecialty(request.getSpecialty());
        profile.setAcceptsOneOnOneCare(request.getAcceptsOneOnOneCare());
        profile.setMaxActiveConsultations(request.getMaxActiveConsultations());
        profile.setAvailabilityJson(request.getAvailabilityJson());
        profile.setTimezone(timezone);

        return mapper.toDoctorCareProfileResponse(profileRepository.save(profile));
    }

    private void validateConsultationManager(UserRole actorRole) {
        if (actorRole == UserRole.SUPER_ADMIN
                || actorRole == UserRole.ADMIN
                || actorRole == UserRole.CARE_COORDINATOR)
            return;
        throw new AppException(ErrorCode.ACCESS_DENIED, "You are not allowed to manage doctor care profiles");
    }

    private void validateDoctor(Long doctorId) {
        UserAccount doctor = userAccountRepository.findById(doctorId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));
        if (doctor.getRole() != UserRole.DOCTOR)
            throw new AppException(ErrorCode.DOCTOR_NOT_FOUND);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
