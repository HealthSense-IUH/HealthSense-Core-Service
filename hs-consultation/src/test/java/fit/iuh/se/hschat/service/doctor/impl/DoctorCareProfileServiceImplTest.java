package fit.iuh.se.hschat.service.doctor.impl;

import fit.iuh.se.hschat.dto.DoctorAvailabilityDto;
import fit.iuh.se.hschat.dto.request.UpdateDoctorAvailabilityRequest;
import fit.iuh.se.hschat.dto.response.DoctorCareProfileResponse;
import fit.iuh.se.hschat.entity.DoctorCareProfile;
import fit.iuh.se.hschat.entity.enums.DoctorSpecialty;
import fit.iuh.se.hschat.mapper.ConsultationMapper;
import fit.iuh.se.hschat.repository.DoctorCareProfileRepository;
import fit.iuh.se.hschat.service.doctor.SupportScheduleValidator;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorCareProfileServiceImplTest {

    @Mock DoctorCareProfileRepository profileRepository;
    @Mock UserAccountRepository userAccountRepository;
    @Mock SupportScheduleValidator scheduleValidator;
    @Mock ConsultationMapper mapper;

    DoctorCareProfileServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DoctorCareProfileServiceImpl(
                profileRepository, userAccountRepository, scheduleValidator, mapper);
    }

    @Test
    void activeDoctorCanReadOwnProfile() {
        DoctorCareProfile profile = profile();
        DoctorCareProfileResponse response = DoctorCareProfileResponse.builder().doctorId(10L).build();
        when(userAccountRepository.findById(10L)).thenReturn(Optional.of(doctor(AccountStatus.ACTIVE)));
        when(profileRepository.findByDoctorId(10L)).thenReturn(Optional.of(profile));
        when(mapper.toDoctorCareProfileResponse(profile)).thenReturn(response);

        assertEquals(response, service.getOwnProfile(10L));
    }

    @Test
    void doctorCanReplaceOnlyOwnAvailabilityAndTimezone() {
        DoctorCareProfile profile = profile();
        var availability = DoctorAvailabilityDto.builder()
                .weekly(List.of(DoctorAvailabilityDto.WeeklySlot.builder()
                        .dayOfWeek("MONDAY")
                        .start("08:00")
                        .end("17:00")
                        .build()))
                .build();
        var request = new UpdateDoctorAvailabilityRequest(availability, " Asia/Bangkok ");
        DoctorCareProfileResponse response = DoctorCareProfileResponse.builder().doctorId(10L).build();
        when(userAccountRepository.findByIdForUpdate(10L))
                .thenReturn(Optional.of(doctor(AccountStatus.ACTIVE)));
        when(profileRepository.findByDoctorIdForUpdate(10L)).thenReturn(Optional.of(profile));
        when(profileRepository.save(profile)).thenReturn(profile);
        when(mapper.toDoctorCareProfileResponse(profile)).thenReturn(response);

        assertEquals(response, service.updateOwnAvailability(10L, request));

        assertEquals("Asia/Bangkok", profile.getTimezone());
        assertTrue(profile.getAvailabilityJson().contains("MONDAY"));
        assertEquals(DoctorSpecialty.CARDIOLOGY, profile.getSpecialty());
        assertEquals(3, profile.getMaxActiveConsultations());
        verify(scheduleValidator).validate(
                argThat(json -> json.contains("MONDAY")),
                org.mockito.ArgumentMatchers.eq("Asia/Bangkok"),
                org.mockito.ArgumentMatchers.eq(true));
        verify(profileRepository).save(profile);
    }

    @Test
    void inactiveAccountCannotUpdateDoctorProfile() {
        when(userAccountRepository.findByIdForUpdate(10L))
                .thenReturn(Optional.of(doctor(AccountStatus.INACTIVE)));

        AppException exception = assertThrows(AppException.class, () -> service.updateOwnAvailability(
                10L,
                new UpdateDoctorAvailabilityRequest(
                        DoctorAvailabilityDto.builder().weekly(List.of()).build(),
                        "Asia/Ho_Chi_Minh")));

        assertEquals(ErrorCode.DOCTOR_NOT_FOUND, exception.getErrorCode());
        verify(profileRepository, never()).findByDoctorIdForUpdate(any());
    }

    private DoctorCareProfile profile() {
        return DoctorCareProfile.builder()
                .id(1L)
                .doctorId(10L)
                .specialty(DoctorSpecialty.CARDIOLOGY)
                .acceptsOneOnOneCare(true)
                .maxActiveConsultations(3)
                .availabilityJson("{\"weekly\":[]}")
                .timezone("Asia/Ho_Chi_Minh")
                .build();
    }

    private UserAccount doctor(AccountStatus status) {
        return UserAccount.builder()
                .id(10L)
                .email("doctor@example.com")
                .passwordHash("hash")
                .role(UserRole.DOCTOR)
                .status(status)
                .build();
    }
}
