package fit.iuh.se.hschat.service.dispatch.impl;

import fit.iuh.se.hschat.entity.ConsultationDispatchState;
import fit.iuh.se.hschat.entity.ConsultationQueueEntry;
import fit.iuh.se.hschat.entity.DoctorCareProfile;
import fit.iuh.se.hschat.entity.enums.ConsultationQueueStatus;
import fit.iuh.se.hschat.entity.enums.DoctorDispatchStatus;
import fit.iuh.se.hschat.entity.enums.DoctorSpecialty;
import fit.iuh.se.hschat.repository.ConsultationDispatchStateRepository;
import fit.iuh.se.hschat.repository.ConsultationQueueEntryRepository;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hschat.repository.DoctorCareProfileRepository;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import fit.iuh.se.hschat.service.dispatch.offer.DoctorOfferStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorDispatchSelectionServiceImplTest {

    @Mock DoctorCareProfileRepository profileRepository;
    @Mock ConsultationQueueEntryRepository queueEntryRepository;
    @Mock ConsultationDispatchStateRepository dispatchStateRepository;
    @Mock ConsultationSessionRepository sessionRepository;
    @Mock UserAccountRepository userAccountRepository;
    @Mock DoctorOfferStore offerStore;

    DoctorDispatchSelectionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new DoctorDispatchSelectionServiceImpl(profileRepository, queueEntryRepository,
                dispatchStateRepository, sessionRepository, userAccountRepository, offerStore);
    }

    @Test
    void noAvailableDoctorsReturnsNoSelectionWithoutTouchingQueue() {
        when(profileRepository.findByDispatchStatusOrderByDoctorIdAsc(DoctorDispatchStatus.AVAILABLE))
                .thenReturn(List.of());

        assertTrue(service.previewNextEligibleDoctor().isEmpty());

        verify(dispatchStateRepository, never()).save(any());
        verify(queueEntryRepository, never()).save(any());
    }

    @Test
    void oneAvailableDoctorIsSelected() {
        stubEligibleDoctors(List.of(profile(10L)));
        when(dispatchStateRepository.findById(1L)).thenReturn(Optional.of(state(null)));

        assertEquals(10L, service.previewNextEligibleDoctor().orElseThrow().getDoctorId());
    }

    @Test
    void stableAscendingOrderAndPointerChooseImmediateNextDoctor() {
        stubEligibleDoctors(List.of(profile(30L), profile(10L), profile(20L)));
        when(dispatchStateRepository.findById(1L)).thenReturn(Optional.of(state(10L)));

        assertEquals(20L, service.previewNextEligibleDoctor().orElseThrow().getDoctorId());
    }

    @Test
    void pointerWrapsToFirstEligibleDoctor() {
        stubEligibleDoctors(List.of(profile(10L), profile(20L), profile(30L)));
        when(dispatchStateRepository.findById(1L)).thenReturn(Optional.of(state(30L)));

        assertEquals(10L, service.previewNextEligibleDoctor().orElseThrow().getDoctorId());
    }

    @Test
    void inactiveAccountAndDoctorWithLegacyWorkloadAreSkipped() {
        List<DoctorCareProfile> profiles = List.of(profile(10L), profile(20L), profile(30L));
        when(profileRepository.findByDispatchStatusOrderByDoctorIdAsc(DoctorDispatchStatus.AVAILABLE))
                .thenReturn(profiles);
        UserAccount inactive = doctor(10L);
        inactive.setStatus(AccountStatus.INACTIVE);
        when(userAccountRepository.findByIdIn(anyCollection()))
                .thenReturn(List.of(inactive, doctor(20L), doctor(30L)));
        when(sessionRepository.findDistinctDoctorIdsByStatusIn(anyCollection())).thenReturn(List.of(20L));
        when(dispatchStateRepository.findById(1L)).thenReturn(Optional.of(state(null)));

        assertEquals(30L, service.previewNextEligibleDoctor().orElseThrow().getDoctorId());
    }

    @Test
    void newlyAvailableDoctorParticipatesOnNextAdvisoryEvaluation() {
        DoctorCareProfile first = profile(10L);
        DoctorCareProfile newlyAvailable = profile(20L);
        when(profileRepository.findByDispatchStatusOrderByDoctorIdAsc(DoctorDispatchStatus.AVAILABLE))
                .thenReturn(List.of(first), List.of(first, newlyAvailable));
        when(userAccountRepository.findByIdIn(anyCollection()))
                .thenReturn(List.of(doctor(10L)), List.of(doctor(10L), doctor(20L)));
        when(sessionRepository.findDistinctDoctorIdsByStatusIn(anyCollection())).thenReturn(List.of());
        when(dispatchStateRepository.findById(1L)).thenReturn(Optional.of(state(10L)));

        assertEquals(10L, service.previewNextEligibleDoctor().orElseThrow().getDoctorId());
        assertEquals(20L, service.previewNextEligibleDoctor().orElseThrow().getDoctorId());
    }

    @Test
    void specialtyAndLegacyCapacityDoNotAffectQueueSelection() {
        DoctorCareProfile lowerId = profile(10L);
        lowerId.setSpecialty(DoctorSpecialty.CARDIOLOGY);
        lowerId.setMaxActiveConsultations(0);
        DoctorCareProfile higherId = profile(20L);
        higherId.setSpecialty(DoctorSpecialty.GENERAL_PRACTICE);
        higherId.setMaxActiveConsultations(99);
        stubEligibleDoctors(List.of(higherId, lowerId));
        when(dispatchStateRepository.findById(1L)).thenReturn(Optional.of(state(null)));

        assertEquals(10L, service.previewNextEligibleDoctor().orElseThrow().getDoctorId());
    }

    @Test
    void redisHeldAvailableDoctorIsSkipped() {
        stubEligibleDoctors(List.of(profile(10L), profile(20L)));
        when(offerStore.heldDoctorIds(anyCollection())).thenReturn(java.util.Set.of(10L));
        when(dispatchStateRepository.findById(1L)).thenReturn(Optional.of(state(null)));

        assertEquals(20L, service.previewNextEligibleDoctor().orElseThrow().getDoctorId());
    }

    @Test
    void selectorEvaluationDoesNotAdvancePointerOrPersistAssignment() {
        ConsultationDispatchState state = state(10L);
        stubEligibleDoctors(List.of(profile(10L), profile(20L)));
        when(dispatchStateRepository.findById(1L)).thenReturn(Optional.of(state));

        assertEquals(20L, service.previewNextEligibleDoctor().orElseThrow().getDoctorId());

        assertEquals(10L, state.getLastDoctorId());
        verify(dispatchStateRepository, never()).save(any());
        verify(queueEntryRepository, never()).save(any());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void fifoFoundationReturnsEarliestWaitingEntryWithoutMutation() {
        ConsultationQueueEntry earliest = queueEntry(1L, LocalDate.of(2026, 9, 5), 7L);
        when(queueEntryRepository.findFirstByStatusOrderByQueueDateAscQueueNumberAsc(
                ConsultationQueueStatus.WAITING)).thenReturn(Optional.of(earliest));

        ConsultationQueueEntry selected = service.previewNextWaitingQueueEntry().orElseThrow();

        assertSame(earliest, selected);
        assertEquals(7L, selected.getQueueNumber());
        assertEquals(ConsultationQueueStatus.WAITING, selected.getStatus());
        verify(queueEntryRepository, never()).save(any());
    }

    @Test
    void orchestrationReturnsAdvisoryQueueAndDoctorCandidateOnly() {
        ConsultationQueueEntry earliest = queueEntry(1L, LocalDate.of(2026, 9, 5), 7L);
        when(queueEntryRepository.findFirstByStatusOrderByQueueDateAscQueueNumberAsc(
                ConsultationQueueStatus.WAITING)).thenReturn(Optional.of(earliest));
        stubEligibleDoctors(List.of(profile(20L)));
        when(dispatchStateRepository.findById(1L)).thenReturn(Optional.of(state(null)));

        var candidate = service.inspectNextDispatchCandidate().orElseThrow();

        assertEquals(1L, candidate.queueEntryId());
        assertEquals(20L, candidate.doctorId());
        verify(queueEntryRepository, never()).save(any());
        verify(dispatchStateRepository, never()).save(any());
    }

    @Test
    void stalePreviewIsRejectedByLockedEligibilityRevalidation() {
        DoctorCareProfile stale = profile(20L);
        stale.setDispatchStatus(DoctorDispatchStatus.UNAVAILABLE);
        when(userAccountRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(doctor(20L)));
        when(profileRepository.findByDoctorIdForUpdate(20L)).thenReturn(Optional.of(stale));

        assertTrue(service.lockAndRevalidateEligibleDoctor(20L).isEmpty());
    }

    @Test
    void explicitCommittedOfferHelperAdvancesOnlySuppliedLockedSingleton() {
        ConsultationDispatchState state = state(10L);
        when(dispatchStateRepository.save(state)).thenReturn(state);

        service.advanceLockedPointerAfterOfferCommit(state, 20L);

        assertEquals(20L, state.getLastDoctorId());
        verify(dispatchStateRepository).save(state);
    }

    private void stubEligibleDoctors(List<DoctorCareProfile> profiles) {
        when(profileRepository.findByDispatchStatusOrderByDoctorIdAsc(DoctorDispatchStatus.AVAILABLE))
                .thenReturn(profiles);
        when(userAccountRepository.findByIdIn(anyCollection()))
                .thenReturn(profiles.stream().map(profile -> doctor(profile.getDoctorId())).toList());
        when(sessionRepository.findDistinctDoctorIdsByStatusIn(anyCollection())).thenReturn(List.of());
        when(offerStore.heldDoctorIds(anyCollection())).thenReturn(java.util.Set.of());
    }

    private DoctorCareProfile profile(Long doctorId) {
        return DoctorCareProfile.builder()
                .id(doctorId + 100)
                .doctorId(doctorId)
                .dispatchStatus(DoctorDispatchStatus.AVAILABLE)
                .stopAfterCurrentSession(false)
                .build();
    }

    private UserAccount doctor(Long id) {
        return UserAccount.builder().id(id).email(id + "@example.com").passwordHash("hash")
                .role(UserRole.DOCTOR).status(AccountStatus.ACTIVE).build();
    }

    private ConsultationDispatchState state(Long lastDoctorId) {
        return ConsultationDispatchState.builder().id(1L).lastDoctorId(lastDoctorId).build();
    }

    private ConsultationQueueEntry queueEntry(Long id, LocalDate date, Long number) {
        return ConsultationQueueEntry.builder()
                .id(id).requestId(id + 1000).memberId(id + 2000)
                .queueDate(date).queueNumber(number).status(ConsultationQueueStatus.WAITING)
                .queuedAt(Instant.parse("2026-09-05T00:00:00Z")).build();
    }
}
