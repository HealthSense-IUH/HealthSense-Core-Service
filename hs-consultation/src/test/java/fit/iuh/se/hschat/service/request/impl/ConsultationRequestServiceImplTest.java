package fit.iuh.se.hschat.service.request.impl;

import fit.iuh.se.hschat.dto.request.ApproveConsultationRequest;
import fit.iuh.se.hschat.dto.request.CreateConsultationRequest;
import fit.iuh.se.hschat.dto.response.ConsultationRequestResponse;
import fit.iuh.se.hschat.dto.response.DoctorCandidateResponse;
import fit.iuh.se.hschat.entity.CareServicePackage;
import fit.iuh.se.hschat.entity.ConsultationRequest;
import fit.iuh.se.hschat.entity.DoctorCareProfile;
import fit.iuh.se.hschat.entity.enums.CareServicePackageStatus;
import fit.iuh.se.hschat.entity.enums.ConsultationRequestStatus;
import fit.iuh.se.hschat.entity.enums.DoctorIneligibilityReason;
import fit.iuh.se.hschat.entity.enums.DoctorSpecialty;
import fit.iuh.se.hschat.mapper.ConsultationMapper;
import fit.iuh.se.hschat.repository.CareServicePackageRepository;
import fit.iuh.se.hschat.repository.ConsultationRequestRepository;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hschat.repository.DoctorCareProfileRepository;
import fit.iuh.se.hschat.service.doctor.SupportScheduleValidator;
import fit.iuh.se.hshealthrecord.repository.HealthRecordRepository;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultationRequestServiceImplTest {

    @Mock
    ConsultationRequestRepository requestRepository;
    @Mock
    ConsultationSessionRepository sessionRepository;
    @Mock
    HealthRecordRepository healthRecordRepository;
    @Mock
    UserAccountRepository userAccountRepository;
    @Mock
    CareServicePackageRepository packageRepository;
    @Mock
    DoctorCareProfileRepository doctorCareProfileRepository;
    @Mock
    SupportScheduleValidator scheduleValidator;
    @Mock
    ConsultationMapper mapper;

    ConsultationRequestServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ConsultationRequestServiceImpl(
                requestRepository,
                sessionRepository,
                healthRecordRepository,
                userAccountRepository,
                packageRepository,
                doctorCareProfileRepository,
                scheduleValidator,
                mapper
        );
        ReflectionTestUtils.setField(service, "paymentDeadlineMinutes", 30L);
    }

    @Test
    void createRequestStoresPackageSnapshotAndPendingReview() {
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(user(1L, UserRole.MEMBER)));
        when(sessionRepository.existsByMemberIdAndStatusIn(eq(1L), anyCollection())).thenReturn(false);
        when(requestRepository.existsByMemberIdAndStatusIn(eq(1L), anyCollection())).thenReturn(false);
        when(packageRepository.findByIdAndStatus(10L, CareServicePackageStatus.ACTIVE))
                .thenReturn(Optional.of(carePackage()));
        when(requestRepository.save(any(ConsultationRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toRequestResponse(any(ConsultationRequest.class)))
                .thenReturn(ConsultationRequestResponse.builder().status(ConsultationRequestStatus.PENDING_REVIEW).build());

        CreateConsultationRequest request = new CreateConsultationRequest();
        request.setPackageId(10L);
        request.setReason("Need monitoring");

        service.createRequest(1L, request);

        ArgumentCaptor<ConsultationRequest> captor = ArgumentCaptor.forClass(ConsultationRequest.class);
        verify(requestRepository).save(captor.capture());
        ConsultationRequest saved = captor.getValue();
        assertEquals(ConsultationRequestStatus.PENDING_REVIEW, saved.getStatus());
        assertEquals(10L, saved.getPackageId());
        assertEquals(new BigDecimal("399000.00"), saved.getPackagePriceSnapshot());
        assertEquals(7, saved.getPackageDurationDaysSnapshot());
    }

    @Test
    void createRequestRejectsMemberWithUnresolvedRequest() {
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(user(1L, UserRole.MEMBER)));
        when(sessionRepository.existsByMemberIdAndStatusIn(eq(1L), anyCollection())).thenReturn(false);
        when(requestRepository.existsByMemberIdAndStatusIn(eq(1L), anyCollection())).thenReturn(true);

        CreateConsultationRequest request = new CreateConsultationRequest();
        request.setPackageId(10L);
        request.setReason("Need monitoring");

        assertThrows(AppException.class, () -> service.createRequest(1L, request));
        verify(packageRepository, never()).findByIdAndStatus(anyLong(), any());
        verify(requestRepository, never()).save(any());
    }

    @Test
    void approveRequestReservesDoctorWithoutCreatingSession() {
        ConsultationRequest existing = ConsultationRequest.builder()
                .id(100L)
                .memberId(1L)
                .status(ConsultationRequestStatus.PENDING_REVIEW)
                .build();

        when(requestRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(user(1L, UserRole.MEMBER)));
        when(userAccountRepository.findById(2L)).thenReturn(Optional.of(user(2L, UserRole.DOCTOR)));
        when(doctorCareProfileRepository.findByDoctorId(2L)).thenReturn(Optional.of(doctorProfile(5)));
        when(scheduleValidator.isValid(anyString(), anyString(), eq(true))).thenReturn(true);
        when(sessionRepository.existsByMemberIdAndStatusIn(eq(1L), anyCollection())).thenReturn(false);
        when(sessionRepository.countByDoctorIdAndStatusIn(eq(2L), anyCollection())).thenReturn(2L);
        when(requestRepository.countByAssignedDoctorIdAndStatusAndPaymentDeadlineAfter(
                eq(2L),
                eq(ConsultationRequestStatus.WAITING_PAYMENT),
                any(Instant.class))
        ).thenReturn(1L);
        when(requestRepository.save(any(ConsultationRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toRequestResponse(any(ConsultationRequest.class)))
                .thenReturn(ConsultationRequestResponse.builder().status(ConsultationRequestStatus.WAITING_PAYMENT).build());

        ApproveConsultationRequest request = new ApproveConsultationRequest();
        request.setDoctorId(2L);

        service.approveRequest(9L, UserRole.CARE_COORDINATOR, 100L, request);

        ArgumentCaptor<ConsultationRequest> captor = ArgumentCaptor.forClass(ConsultationRequest.class);
        verify(requestRepository).save(captor.capture());
        ConsultationRequest saved = captor.getValue();
        assertEquals(ConsultationRequestStatus.WAITING_PAYMENT, saved.getStatus());
        assertEquals(2L, saved.getAssignedDoctorId());
        assertNotNull(saved.getDoctorReservedAt());
        assertNotNull(saved.getPaymentDeadline());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void approveRequestCountsActiveScheduledSessionsAndReservationsForCapacity() {
        ConsultationRequest existing = ConsultationRequest.builder()
                .id(100L)
                .memberId(1L)
                .status(ConsultationRequestStatus.PENDING_REVIEW)
                .build();

        when(requestRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(user(1L, UserRole.MEMBER)));
        when(userAccountRepository.findById(2L)).thenReturn(Optional.of(user(2L, UserRole.DOCTOR)));
        when(doctorCareProfileRepository.findByDoctorId(2L)).thenReturn(Optional.of(doctorProfile(5)));
        when(scheduleValidator.isValid(anyString(), anyString(), eq(true))).thenReturn(true);
        when(sessionRepository.countByDoctorIdAndStatusIn(eq(2L), anyCollection())).thenReturn(4L);
        when(requestRepository.countByAssignedDoctorIdAndStatusAndPaymentDeadlineAfter(
                eq(2L),
                eq(ConsultationRequestStatus.WAITING_PAYMENT),
                any(Instant.class))
        ).thenReturn(1L);

        ApproveConsultationRequest request = new ApproveConsultationRequest();
        request.setDoctorId(2L);

        assertThrows(AppException.class, () -> service.approveRequest(9L, UserRole.CARE_COORDINATOR, 100L, request));
        verify(requestRepository, never()).save(any());
    }

    @Test
    void approveRequestDoesNotCountExpiredWaitingPaymentReservationsForCapacity() {
        ConsultationRequest existing = ConsultationRequest.builder()
                .id(100L)
                .memberId(1L)
                .status(ConsultationRequestStatus.PENDING_REVIEW)
                .build();

        when(requestRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(user(1L, UserRole.MEMBER)));
        when(userAccountRepository.findById(2L)).thenReturn(Optional.of(user(2L, UserRole.DOCTOR)));
        when(doctorCareProfileRepository.findByDoctorId(2L)).thenReturn(Optional.of(doctorProfile(2)));
        when(scheduleValidator.isValid(anyString(), anyString(), eq(true))).thenReturn(true);
        when(sessionRepository.existsByMemberIdAndStatusIn(eq(1L), anyCollection())).thenReturn(false);
        when(sessionRepository.countByDoctorIdAndStatusIn(eq(2L), anyCollection())).thenReturn(1L);
        when(requestRepository.countByAssignedDoctorIdAndStatusAndPaymentDeadlineAfter(
                eq(2L),
                eq(ConsultationRequestStatus.WAITING_PAYMENT),
                any(Instant.class))
        ).thenReturn(0L);
        when(requestRepository.save(any(ConsultationRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toRequestResponse(any(ConsultationRequest.class)))
                .thenReturn(ConsultationRequestResponse.builder().status(ConsultationRequestStatus.WAITING_PAYMENT).build());

        ApproveConsultationRequest request = new ApproveConsultationRequest();
        request.setDoctorId(2L);

        service.approveRequest(9L, UserRole.CARE_COORDINATOR, 100L, request);

        verify(requestRepository, atLeastOnce()).countByAssignedDoctorIdAndStatusAndPaymentDeadlineAfter(
                eq(2L),
                eq(ConsultationRequestStatus.WAITING_PAYMENT),
                any(Instant.class)
        );
        verify(requestRepository).save(any(ConsultationRequest.class));
    }

    @Test
    void approveRequestCalculatesPaymentDeadlineOnBackend() {
        ConsultationRequest existing = ConsultationRequest.builder()
                .id(100L)
                .memberId(1L)
                .status(ConsultationRequestStatus.PENDING_REVIEW)
                .build();

        when(requestRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(user(1L, UserRole.MEMBER)));
        when(userAccountRepository.findById(2L)).thenReturn(Optional.of(user(2L, UserRole.DOCTOR)));
        when(doctorCareProfileRepository.findByDoctorId(2L)).thenReturn(Optional.of(doctorProfile(5)));
        when(scheduleValidator.isValid(anyString(), anyString(), eq(true))).thenReturn(true);
        when(sessionRepository.existsByMemberIdAndStatusIn(eq(1L), anyCollection())).thenReturn(false);
        when(sessionRepository.countByDoctorIdAndStatusIn(eq(2L), anyCollection())).thenReturn(0L);
        when(requestRepository.countByAssignedDoctorIdAndStatusAndPaymentDeadlineAfter(
                eq(2L),
                eq(ConsultationRequestStatus.WAITING_PAYMENT),
                any(Instant.class))
        ).thenReturn(0L);
        when(requestRepository.save(any(ConsultationRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toRequestResponse(any(ConsultationRequest.class)))
                .thenReturn(ConsultationRequestResponse.builder().status(ConsultationRequestStatus.WAITING_PAYMENT).build());

        ApproveConsultationRequest request = new ApproveConsultationRequest();
        request.setDoctorId(2L);

        service.approveRequest(9L, UserRole.CARE_COORDINATOR, 100L, request);

        ArgumentCaptor<ConsultationRequest> captor = ArgumentCaptor.forClass(ConsultationRequest.class);
        verify(requestRepository).save(captor.capture());
        assertEquals(30L, ChronoUnit.MINUTES.between(
                captor.getValue().getDoctorReservedAt(),
                captor.getValue().getPaymentDeadline()
        ));
    }

    @Test
    void doctorCandidatesKeepPreferredDoctorVisibleWhenIneligible() {
        ConsultationRequest existing = ConsultationRequest.builder()
                .id(100L)
                .memberId(1L)
                .preferredDoctorId(2L)
                .status(ConsultationRequestStatus.PENDING_REVIEW)
                .build();
        UserAccount preferredDoctor = user(2L, UserRole.DOCTOR);

        when(requestRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(userAccountRepository.findDoctors(
                eq(UserRole.DOCTOR),
                eq(AccountStatus.ACTIVE),
                any(PageRequest.class))
        ).thenReturn(new PageImpl<>(List.of(preferredDoctor), PageRequest.of(0, 10), 1));
        when(doctorCareProfileRepository.findByDoctorIdIn(List.of(2L)))
                .thenReturn(List.of(doctorProfile(1)));
        when(scheduleValidator.isValid(anyString(), anyString(), eq(true))).thenReturn(true);
        when(sessionRepository.countByDoctorIdAndStatusIn(eq(2L), anyCollection())).thenReturn(1L);
        when(requestRepository.countByAssignedDoctorIdAndStatusAndPaymentDeadlineAfter(
                eq(2L),
                eq(ConsultationRequestStatus.WAITING_PAYMENT),
                any(Instant.class))
        ).thenReturn(0L);

        PageResponse<DoctorCandidateResponse> response = service.getDoctorCandidates(
                UserRole.CARE_COORDINATOR,
                100L,
                null,
                null,
                false,
                PageRequest.of(0, 10)
        );

        assertEquals(1, response.getContent().size());
        DoctorCandidateResponse candidate = response.getContent().getFirst();
        assertEquals(2L, candidate.getDoctorId());
        assertTrue(candidate.getPreferredByMember());
        assertFalse(candidate.getEligible());
        assertTrue(candidate.getIneligibleReasons().contains(DoctorIneligibilityReason.CAPACITY_FULL));
        verify(requestRepository, never()).save(any());
    }

    @Test
    void approveRequestRejectsDoctorThatDoesNotAcceptOneOnOneCare() {
        ConsultationRequest existing = ConsultationRequest.builder()
                .id(100L)
                .memberId(1L)
                .status(ConsultationRequestStatus.PENDING_REVIEW)
                .build();

        when(requestRepository.findById(100L)).thenReturn(Optional.of(existing));
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(user(1L, UserRole.MEMBER)));
        when(userAccountRepository.findById(2L)).thenReturn(Optional.of(user(2L, UserRole.DOCTOR)));
        when(doctorCareProfileRepository.findByDoctorId(2L)).thenReturn(Optional.of(doctorProfile(5, false)));
        when(scheduleValidator.isValid(anyString(), anyString(), eq(false))).thenReturn(true);

        ApproveConsultationRequest request = new ApproveConsultationRequest();
        request.setDoctorId(2L);

        assertThrows(AppException.class, () -> service.approveRequest(9L, UserRole.CARE_COORDINATOR, 100L, request));
        verify(requestRepository, never()).save(any());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void approveRequestDtoDoesNotExposePaymentDeadlineToClient() {
        boolean hasPaymentDeadlineField = Arrays.stream(ApproveConsultationRequest.class.getDeclaredFields())
                .anyMatch(field -> "paymentDeadline".equals(field.getName()));

        assertFalse(hasPaymentDeadlineField);
    }

    private UserAccount user(Long id, UserRole role) {
        return UserAccount.builder()
                .id(id)
                .email(id + "@example.com")
                .passwordHash("hash")
                .role(role)
                .status(AccountStatus.ACTIVE)
                .build();
    }

    private CareServicePackage carePackage() {
        return CareServicePackage.builder()
                .id(10L)
                .code("PERSONAL_CARE_7D")
                .name("Personal Care - 7 Days")
                .priceAmount(new BigDecimal("399000.00"))
                .durationDays(7)
                .renewable(true)
                .status(CareServicePackageStatus.ACTIVE)
                .build();
    }

    private DoctorCareProfile doctorProfile(int maxActiveConsultations) {
        return doctorProfile(maxActiveConsultations, true);
    }

    private DoctorCareProfile doctorProfile(int maxActiveConsultations, boolean acceptsOneOnOneCare) {
        return DoctorCareProfile.builder()
                .doctorId(2L)
                .specialty(DoctorSpecialty.CARDIOLOGY)
                .acceptsOneOnOneCare(acceptsOneOnOneCare)
                .maxActiveConsultations(maxActiveConsultations)
                .availabilityJson("{\"weekly\":[{\"dayOfWeek\":\"MONDAY\",\"start\":\"07:00\",\"end\":\"11:00\"}]}")
                .timezone("Asia/Ho_Chi_Minh")
                .build();
    }
}
