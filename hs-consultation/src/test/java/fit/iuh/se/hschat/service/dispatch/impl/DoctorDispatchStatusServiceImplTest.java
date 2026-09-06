package fit.iuh.se.hschat.service.dispatch.impl;

import fit.iuh.se.hschat.dto.request.UpdateDoctorDispatchPreferencesRequest;
import fit.iuh.se.hschat.dto.request.UpdateDoctorDispatchStatusRequest;
import fit.iuh.se.hschat.entity.DoctorCareProfile;
import fit.iuh.se.hschat.entity.enums.DoctorDispatchStatus;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hschat.repository.DoctorCareProfileRepository;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsoperations.entity.enums.BusinessEventType;
import fit.iuh.se.hsoperations.event.OperationalEventPublisher;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.context.ApplicationEventPublisher;
import fit.iuh.se.hschat.service.dispatch.DoctorDispatchSelectionService;
import fit.iuh.se.hschat.service.dispatch.DoctorOfferService;
import fit.iuh.se.hschat.service.dispatch.offer.DoctorOfferStore;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorDispatchStatusServiceImplTest {

    @Mock DoctorCareProfileRepository profileRepository;
    @Mock ConsultationSessionRepository sessionRepository;
    @Mock UserAccountRepository userAccountRepository;
    @Mock OperationalEventPublisher eventPublisher;
    @Mock ApplicationEventPublisher applicationEventPublisher;
    @Mock DoctorDispatchSelectionService selectionService;
    @Mock DoctorOfferStore offerStore;
    @Mock DoctorOfferService offerService;

    DoctorDispatchStatusServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DoctorDispatchStatusServiceImpl(
                profileRepository, sessionRepository, userAccountRepository, eventPublisher,
                applicationEventPublisher, selectionService, offerStore, offerService);
        ReflectionTestUtils.setField(service, "clock",
                Clock.fixed(Instant.parse("2026-09-05T04:00:00Z"), ZoneOffset.UTC));
        lenient().when(profileRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void newProfileDefaultsToUnavailableAndStopAfterCurrentFalse() {
        DoctorCareProfile profile = DoctorCareProfile.builder().doctorId(10L).build();
        when(userAccountRepository.findById(10L)).thenReturn(Optional.of(doctor(10L)));
        when(profileRepository.findByDoctorId(10L)).thenReturn(Optional.of(profile));

        var response = service.getStatus(10L);

        assertEquals(DoctorDispatchStatus.UNAVAILABLE, response.dispatchStatus());
        assertFalse(response.effectivelyDispatchable());
        assertFalse(response.stopAfterCurrentSession());
    }

    @Test
    void doctorCanEnterAvailablePoolAndTimestampIsUpdated() {
        DoctorCareProfile profile = profile(DoctorDispatchStatus.UNAVAILABLE);
        stubLockedDoctor(profile);
        when(sessionRepository.existsByDoctorIdAndStatusIn(eq(10L), anyCollection())).thenReturn(false);

        var response = service.updateStatus(10L,
                new UpdateDoctorDispatchStatusRequest(DoctorDispatchStatus.AVAILABLE));

        assertEquals(DoctorDispatchStatus.AVAILABLE, response.dispatchStatus());
        assertTrue(response.effectivelyDispatchable());
        assertEquals(Instant.parse("2026-09-05T04:00:00Z"), response.dispatchStatusChangedAt());
        verify(eventPublisher).record(argThat(event -> event.eventType() == BusinessEventType.DOCTOR_AVAILABLE
                && event.notifications().isEmpty()));
        verify(applicationEventPublisher).publishEvent(argThat((Object event) ->
                event instanceof fit.iuh.se.hschat.service.dispatch.event.DoctorDispatchStatusChanged changed
                        && changed.currentStatus() == DoctorDispatchStatus.AVAILABLE));
    }

    @Test
    void doctorCanLeaveAvailablePool() {
        DoctorCareProfile profile = profile(DoctorDispatchStatus.AVAILABLE);
        stubLockedDoctor(profile);

        var response = service.updateStatus(10L,
                new UpdateDoctorDispatchStatusRequest(DoctorDispatchStatus.UNAVAILABLE));

        assertEquals(DoctorDispatchStatus.UNAVAILABLE, response.dispatchStatus());
        verify(eventPublisher).record(argThat(event -> event.eventType() == BusinessEventType.DOCTOR_UNAVAILABLE));
        verify(sessionRepository, never()).existsByDoctorIdAndStatusIn(anyLong(), anyCollection());
    }

    @Test
    void clientCannotManuallySetBusy() {
        DoctorCareProfile profile = profile(DoctorDispatchStatus.UNAVAILABLE);
        stubLockedDoctor(profile);

        assertThrows(AppException.class, () -> service.updateStatus(10L,
                new UpdateDoctorDispatchStatusRequest(DoctorDispatchStatus.BUSY)));

        verify(profileRepository, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = DoctorDispatchStatus.class, names = {"AVAILABLE", "UNAVAILABLE"})
    void busyDoctorCannotBeManuallyToggled(DoctorDispatchStatus target) {
        DoctorCareProfile profile = profile(DoctorDispatchStatus.BUSY);
        profile.setBusySessionId(99L);
        stubLockedDoctor(profile);

        assertThrows(AppException.class, () -> service.updateStatus(10L,
                new UpdateDoctorDispatchStatusRequest(target)));

        assertEquals(DoctorDispatchStatus.BUSY, profile.getDispatchStatus());
        assertEquals(99L, profile.getBusySessionId());
    }

    @Test
    void incompatibleLegacyWorkloadBlocksEnteringPool() {
        DoctorCareProfile profile = profile(DoctorDispatchStatus.UNAVAILABLE);
        stubLockedDoctor(profile);
        when(sessionRepository.existsByDoctorIdAndStatusIn(eq(10L), anyCollection())).thenReturn(true);

        assertThrows(AppException.class, () -> service.updateStatus(10L,
                new UpdateDoctorDispatchStatusRequest(DoctorDispatchStatus.AVAILABLE)));

        assertEquals(DoctorDispatchStatus.UNAVAILABLE, profile.getDispatchStatus());
    }

    @Test
    void nonDoctorOrInactiveAccountCannotUseSelfServiceMutation() {
        UserAccount otherUser = doctor(10L);
        otherUser.setRole(UserRole.MEMBER);
        when(userAccountRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(otherUser));

        assertThrows(AppException.class, () -> service.updateStatus(10L,
                new UpdateDoctorDispatchStatusRequest(DoctorDispatchStatus.AVAILABLE)));

        verify(profileRepository, never()).findByDoctorIdForUpdate(anyLong());
    }

    @Test
    void doctorCanSetAndUnsetStopAfterCurrentSession() {
        DoctorCareProfile profile = profile(DoctorDispatchStatus.AVAILABLE);
        stubLockedDoctor(profile);

        assertTrue(service.updatePreferences(10L,
                new UpdateDoctorDispatchPreferencesRequest(true)).stopAfterCurrentSession());
        assertFalse(service.updatePreferences(10L,
                new UpdateDoctorDispatchPreferencesRequest(false)).stopAfterCurrentSession());

        assertEquals(DoctorDispatchStatus.AVAILABLE, profile.getDispatchStatus());
        verify(eventPublisher, times(2)).record(argThat(event ->
                event.eventType() == BusinessEventType.DOCTOR_STOP_AFTER_CURRENT_CHANGED));
    }

    @Test
    void changingPreferenceWhileBusyDoesNotReleaseOrChangeDoctorStatus() {
        DoctorCareProfile profile = profile(DoctorDispatchStatus.BUSY);
        profile.setBusySessionId(99L);
        Instant statusChangedAt = profile.getDispatchStatusChangedAt();
        stubLockedDoctor(profile);

        var response = service.updatePreferences(10L,
                new UpdateDoctorDispatchPreferencesRequest(true));

        assertEquals(DoctorDispatchStatus.BUSY, response.dispatchStatus());
        assertEquals(99L, response.busySessionId());
        assertEquals(statusChangedAt, response.dispatchStatusChangedAt());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void profileLockSerializesSuccessiveStatusToggles() {
        DoctorCareProfile profile = profile(DoctorDispatchStatus.UNAVAILABLE);
        stubLockedDoctor(profile);
        when(sessionRepository.existsByDoctorIdAndStatusIn(eq(10L), anyCollection())).thenReturn(false);

        service.updateStatus(10L, new UpdateDoctorDispatchStatusRequest(DoctorDispatchStatus.AVAILABLE));
        service.updateStatus(10L, new UpdateDoctorDispatchStatusRequest(DoctorDispatchStatus.UNAVAILABLE));

        assertEquals(DoctorDispatchStatus.UNAVAILABLE, profile.getDispatchStatus());
        verify(userAccountRepository, times(2)).findByIdForUpdate(10L);
        verify(profileRepository, times(2)).findByDoctorIdForUpdate(10L);
    }

    private void stubLockedDoctor(DoctorCareProfile profile) {
        when(userAccountRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(doctor(10L)));
        when(profileRepository.findByDoctorIdForUpdate(10L)).thenReturn(Optional.of(profile));
    }

    private DoctorCareProfile profile(DoctorDispatchStatus status) {
        return DoctorCareProfile.builder()
                .id(1L)
                .doctorId(10L)
                .dispatchStatus(status)
                .stopAfterCurrentSession(false)
                .dispatchStatusChangedAt(Instant.parse("2026-09-01T00:00:00Z"))
                .build();
    }

    private UserAccount doctor(Long id) {
        return UserAccount.builder()
                .id(id)
                .email(id + "@example.com")
                .passwordHash("hash")
                .role(UserRole.DOCTOR)
                .status(AccountStatus.ACTIVE)
                .build();
    }
}
