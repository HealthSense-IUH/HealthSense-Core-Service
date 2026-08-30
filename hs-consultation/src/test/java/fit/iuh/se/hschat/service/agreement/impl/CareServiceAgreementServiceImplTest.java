package fit.iuh.se.hschat.service.agreement.impl;

import fit.iuh.se.hschat.entity.*;
import fit.iuh.se.hschat.entity.enums.*;
import fit.iuh.se.hschat.repository.*;
import fit.iuh.se.hsshared.advice.entity.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CareServiceAgreementServiceImplTest {

    @Mock CareServiceAgreementRepository agreementRepository;
    @Mock ConsultationRequestRepository requestRepository;
    @Mock CareServicePackageRepository packageRepository;
    @Mock DoctorCareProfileRepository profileRepository;
    @Mock ConsultationRenewalRepository renewalRepository;
    @Mock fit.iuh.se.hsoperations.service.OperationalEventService operationalEventService;

    CareServiceAgreementServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CareServiceAgreementServiceImpl(
                agreementRepository, requestRepository, packageRepository, profileRepository, renewalRepository,
                operationalEventService);
    }

    @Test
    void assignmentCreatesCompleteImmutableOfferSnapshot() {
        ConsultationRequest request = assignedRequest();
        CareServicePackage source = carePackage("Cardiac Care V1", new BigDecimal("100000"));
        when(packageRepository.findById(10L)).thenReturn(Optional.of(source));
        when(profileRepository.findByDoctorId(2L)).thenReturn(Optional.of(profile()));
        when(agreementRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CareServiceAgreement agreement = service.createForReservation(request);
        source.setName("Cardiac Care V2 catalog mutation");
        source.setPriceAmount(new BigDecimal("200000"));

        assertEquals("Cardiac Care V1", agreement.getPackageName());
        assertEquals(new BigDecimal("100000"), agreement.getPriceAmount());
        assertEquals(List.of(CareServiceCode.SECURE_MESSAGING), agreement.getIncludedServices());
        assertEquals(CareServiceAgreementStatus.PENDING_ACCEPTANCE, agreement.getStatus());
        assertEquals(profile().getAvailabilityJson(), agreement.getSupportScheduleSnapshotJson());
        assertNotNull(agreement.getTermsPolicyReference());
        assertNotNull(agreement.getEmergencyLimitation());
        assertNotNull(agreement.getHealthDataScopeDisclosure());
    }

    @Test
    void onlyOwningMemberCanAcceptAgreement() {
        ConsultationRequest request = assignedRequest();
        when(requestRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(request));

        assertThrows(AppException.class, () -> service.accept(99L, 100L, 700L));
        verify(agreementRepository, never()).findByIdAndMemberId(anyLong(), anyLong());
    }

    @Test
    void explicitAcceptanceTransitionsAgreementAndRequestExactlyOnce() {
        ConsultationRequest request = assignedRequest();
        CareServiceAgreement agreement = pendingAgreement();
        when(requestRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(request));
        when(agreementRepository.findByIdAndMemberId(700L, 1L)).thenReturn(Optional.of(agreement));

        var response = service.accept(1L, 100L, 700L);

        assertEquals(CareServiceAgreementStatus.ACCEPTED, response.getStatus());
        assertEquals(1L, agreement.getAcceptedByMember());
        assertNotNull(agreement.getAcceptedAt());
        assertEquals(ConsultationRequestStatus.WAITING_PAYMENT, request.getStatus());
        verify(operationalEventService).record(argThat(command ->
                command.eventType() == fit.iuh.se.hsoperations.entity.enums.BusinessEventType.AGREEMENT_ACCEPTED
                        && Long.valueOf(1L).equals(command.actorUserId())
                        && "MEMBER".equals(command.actorRole())
                        && "ACCEPTED".equals(command.newState())));

        assertThrows(AppException.class, () -> service.accept(1L, 100L, 700L));
        assertEquals("Cardiac Care V1", agreement.getPackageName());
        assertEquals(new BigDecimal("100000"), agreement.getPriceAmount());
    }

    @Test
    void doctorReplacementInvalidatesAcceptedAgreementAndCreatesReacceptanceOffer() {
        ConsultationRequest replacementRequest = assignedRequest();
        replacementRequest.setAssignedDoctorId(3L);
        CareServiceAgreement oldAgreement = pendingAgreement();
        oldAgreement.setStatus(CareServiceAgreementStatus.ACCEPTED);
        when(agreementRepository.findFirstByRequestIdAndStatusInOrderByCreatedAtDesc(
                eq(100L), anyCollection())).thenReturn(Optional.of(oldAgreement));
        when(packageRepository.findById(10L)).thenReturn(Optional.of(carePackage(
                "Cardiac Care V1", new BigDecimal("100000"))));
        DoctorCareProfile replacementProfile = profile();
        replacementProfile.setDoctorId(3L);
        replacementProfile.setAvailabilityJson("{\"weekly\":[{\"dayOfWeek\":\"TUESDAY\"}]}");
        when(profileRepository.findByDoctorId(3L)).thenReturn(Optional.of(replacementProfile));
        when(agreementRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CareServiceAgreement replacement = service.createForReservation(replacementRequest);

        assertEquals(CareServiceAgreementStatus.INVALIDATED, oldAgreement.getStatus());
        assertNotNull(oldAgreement.getInvalidatedAt());
        assertEquals(CareServiceAgreementStatus.PENDING_ACCEPTANCE, replacement.getStatus());
        assertEquals(3L, replacement.getDoctorId());
        assertNull(replacement.getAcceptedAt());
    }

    @Test
    void renewalNeedsExplicitMemberAcceptanceBeforeWaitingPayment() {
        ConsultationRenewal renewal = ConsultationRenewal.builder().id(800L).sessionId(900L)
                .memberId(1L).doctorId(2L).packageFamilyId(50L)
                .status(ConsultationRenewalStatus.PENDING_ACCEPTANCE)
                .requestedAt(Instant.now()).build();
        CareServiceAgreement agreement = CareServiceAgreement.builder().id(801L).renewalId(800L)
                .agreementType(CareServiceAgreementType.RENEWAL).memberId(1L).doctorId(2L)
                .status(CareServiceAgreementStatus.PENDING_ACCEPTANCE)
                .validUntil(Instant.now().plusSeconds(600)).build();
        when(renewalRepository.findByIdForUpdate(800L)).thenReturn(Optional.of(renewal));
        when(agreementRepository.findByIdAndMemberId(801L, 1L)).thenReturn(Optional.of(agreement));

        var response = service.acceptRenewal(1L, 800L, 801L);

        assertEquals(CareServiceAgreementStatus.ACCEPTED, response.getStatus());
        assertEquals(ConsultationRenewalStatus.WAITING_PAYMENT, renewal.getStatus());
        assertEquals(1L, agreement.getAcceptedByMember());
        assertNotNull(agreement.getAcceptedAt());
    }

    private ConsultationRequest assignedRequest() {
        return ConsultationRequest.builder()
                .id(100L)
                .memberId(1L)
                .assignedDoctorId(2L)
                .packageId(10L)
                .packageVersion(1)
                .status(ConsultationRequestStatus.WAITING_ACCEPTANCE)
                .paymentDeadline(Instant.now().plusSeconds(600))
                .build();
    }

    private CareServiceAgreement pendingAgreement() {
        return CareServiceAgreement.builder()
                .id(700L)
                .requestId(100L)
                .memberId(1L)
                .doctorId(2L)
                .packageId(10L)
                .packageFamilyId(20L)
                .packageCode("CARDIAC")
                .packageName("Cardiac Care V1")
                .packageVersion(1)
                .priceAmount(new BigDecimal("100000"))
                .currency("VND")
                .durationDays(30)
                .status(CareServiceAgreementStatus.PENDING_ACCEPTANCE)
                .validUntil(Instant.now().plusSeconds(600))
                .build();
    }

    private CareServicePackage carePackage(String name, BigDecimal price) {
        return CareServicePackage.builder()
                .id(10L)
                .familyId(20L)
                .code("CARDIAC")
                .name(name)
                .versionNumber(1)
                .description("Long-term one-on-one monitoring")
                .priceAmount(price)
                .currency("VND")
                .durationDays(30)
                .includedServices(List.of(CareServiceCode.SECURE_MESSAGING))
                .excludedServices(List.of())
                .supportPolicy(CareServiceSupportPolicy.ASSIGNED_DOCTOR_SUPPORT_SCHEDULE)
                .renewable(true)
                .termsPolicyReference("TERMS_V1")
                .build();
    }

    private DoctorCareProfile profile() {
        return DoctorCareProfile.builder()
                .doctorId(2L)
                .availabilityJson("{\"weekly\":[{\"dayOfWeek\":\"MONDAY\"}]}")
                .timezone("Asia/Ho_Chi_Minh")
                .build();
    }
}
