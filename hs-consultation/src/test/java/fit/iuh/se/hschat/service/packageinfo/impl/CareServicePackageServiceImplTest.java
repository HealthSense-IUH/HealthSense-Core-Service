package fit.iuh.se.hschat.service.packageinfo.impl;

import fit.iuh.se.hschat.dto.request.UpdateCareServicePackageRequest;
import fit.iuh.se.hschat.dto.response.CareServicePackageResponse;
import fit.iuh.se.hschat.entity.CareServicePackage;
import fit.iuh.se.hschat.entity.CareServicePackageFamily;
import fit.iuh.se.hschat.entity.enums.CareServiceCode;
import fit.iuh.se.hschat.entity.enums.CareServicePackageStatus;
import fit.iuh.se.hschat.entity.enums.CareServiceSupportPolicy;
import fit.iuh.se.hschat.entity.enums.DoctorSpecialty;
import fit.iuh.se.hschat.mapper.ConsultationMapper;
import fit.iuh.se.hschat.repository.CareServicePackageFamilyRepository;
import fit.iuh.se.hschat.repository.CareServicePackageRepository;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsoperations.dto.command.OperationalEventCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CareServicePackageServiceImplTest {

    @Mock
    CareServicePackageRepository packageRepository;
    @Mock
    CareServicePackageFamilyRepository familyRepository;
    @Mock
    ConsultationMapper mapper;
    @Mock
    fit.iuh.se.hsoperations.event.OperationalEventPublisher OperationalEventPublisher;

    CareServicePackageServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CareServicePackageServiceImpl(packageRepository, familyRepository, mapper, OperationalEventPublisher);
    }

    @Test
    void activeBrowseUsesOnlyActiveVersions() {
        when(packageRepository.findByStatusOrderByCreatedAtDesc(
                CareServicePackageStatus.ACTIVE,
                PageRequest.of(0, 10)
        )).thenReturn(Page.empty());

        service.getActivePackages(PageRequest.of(0, 10));

        verify(packageRepository).findByStatusOrderByCreatedAtDesc(
                CareServicePackageStatus.ACTIVE,
                PageRequest.of(0, 10)
        );
    }

    @Test
    void inactiveOrRetiredVersionIsUnavailableToMemberLookup() {
        when(packageRepository.findByIdAndStatus(10L, CareServicePackageStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThrows(AppException.class, () -> service.getActivePackage(10L));
    }

    @Test
    void retiredVersionRemainsHistoricallyRetrievableByAdmin() {
        CareServicePackage retired = packageVersion(10L, 1, CareServicePackageStatus.RETIRED);
        CareServicePackageResponse response = CareServicePackageResponse.builder()
                .id(10L)
                .version(1)
                .status(CareServicePackageStatus.RETIRED)
                .build();
        when(packageRepository.findById(10L)).thenReturn(Optional.of(retired));
        when(mapper.toCareServicePackageResponse(retired)).thenReturn(response);

        CareServicePackageResponse result = service.getPackageForAdmin(UserRole.ADMIN, 10L);

        assertEquals(10L, result.getId());
        assertEquals(CareServicePackageStatus.RETIRED, result.getStatus());
    }

    @Test
    void semanticUpdateToActivePublishesNewVersionWithoutChangingOldSemantics() {
        CareServicePackage active = packageVersion(10L, 1, CareServicePackageStatus.ACTIVE);
        BigDecimal oldPrice = active.getPriceAmount();
        Integer oldDuration = active.getDurationDays();
        String oldName = active.getName();
        String oldShortDescription = active.getShortDescription();
        String oldDescription = active.getDescription();
        List<CareServiceCode> oldIncluded = List.copyOf(active.getIncludedServices());
        List<CareServiceCode> oldExcluded = List.copyOf(active.getExcludedServices());
        DoctorSpecialty oldSpecialty = active.getRequiredSpecialty();
        CareServiceSupportPolicy oldSupportPolicy = active.getSupportPolicy();
        Boolean oldRenewable = active.getRenewable();
        String oldTermsReference = active.getTermsPolicyReference();
        when(packageRepository.findById(10L)).thenReturn(Optional.of(active));
        when(familyRepository.findByIdForUpdate(100L))
                .thenReturn(Optional.of(CareServicePackageFamily.builder().id(100L).code("CARDIO").build()));
        when(packageRepository.findTopByFamilyIdOrderByVersionNumberDesc(100L))
                .thenReturn(Optional.of(active));
        when(packageRepository.saveAndFlush(any(CareServicePackage.class))).thenAnswer(invocation -> {
            CareServicePackage saved = invocation.getArgument(0);
            if (saved.getId() == null)
                saved.setId(11L);
            return saved;
        });
        when(mapper.toCareServicePackageResponse(any(CareServicePackage.class))).thenAnswer(invocation -> {
            CareServicePackage saved = invocation.getArgument(0);
            return CareServicePackageResponse.builder()
                    .id(saved.getId())
                    .familyId(saved.getFamilyId())
                    .version(saved.getVersionNumber())
                    .priceAmount(saved.getPriceAmount())
                    .status(saved.getStatus())
                    .build();
        });

        CareServicePackageResponse result = service.updatePackage(
                99L,
                UserRole.ADMIN,
                10L,
                updateRequest()
        );

        ArgumentCaptor<CareServicePackage> versions = ArgumentCaptor.forClass(CareServicePackage.class);
        verify(packageRepository, org.mockito.Mockito.times(2)).saveAndFlush(versions.capture());
        CareServicePackage newVersion = versions.getAllValues().get(1);
        assertEquals(11L, result.getId());
        assertEquals(2, result.getVersion());
        assertEquals(CareServicePackageStatus.ACTIVE, result.getStatus());
        assertEquals(CareServicePackageStatus.RETIRED, active.getStatus());
        assertEquals(oldName, active.getName());
        assertEquals(oldShortDescription, active.getShortDescription());
        assertEquals(oldDescription, active.getDescription());
        assertEquals(oldPrice, active.getPriceAmount());
        assertEquals(oldDuration, active.getDurationDays());
        assertEquals(oldIncluded, active.getIncludedServices());
        assertEquals(oldExcluded, active.getExcludedServices());
        assertEquals(oldSpecialty, active.getRequiredSpecialty());
        assertEquals(oldSupportPolicy, active.getSupportPolicy());
        assertEquals(oldRenewable, active.getRenewable());
        assertEquals(oldTermsReference, active.getTermsPolicyReference());
        assertEquals(new BigDecimal("1290000.00"), newVersion.getPriceAmount());
        assertEquals(14, newVersion.getDurationDays());
        assertEquals(updateRequest().getIncludedServices(), newVersion.getIncludedServices());
        assertEquals(updateRequest().getExcludedServices(), newVersion.getExcludedServices());
        assertEquals(DoctorSpecialty.CARDIOLOGY, newVersion.getRequiredSpecialty());
        assertEquals(
                CareServiceSupportPolicy.ASSIGNED_DOCTOR_SUPPORT_SCHEDULE,
                newVersion.getSupportPolicy()
        );
        assertEquals(true, newVersion.getRenewable());
        assertEquals("terms:v2", newVersion.getTermsPolicyReference());
        assertNotEquals(active.getId(), newVersion.getId());
        ArgumentCaptor<OperationalEventCommand> auditEvents = ArgumentCaptor.forClass(OperationalEventCommand.class);
        verify(OperationalEventPublisher, org.mockito.Mockito.times(2)).record(auditEvents.capture());
        auditEvents.getAllValues().forEach(event -> assertEquals(99L, event.actorUserId()));

        CareServicePackageResponse historical = service.getPackageForAdmin(UserRole.ADMIN, 10L);
        assertEquals(10L, historical.getId());
        assertEquals(1, historical.getVersion());
        assertEquals(oldPrice, historical.getPriceAmount());
        assertEquals(CareServicePackageStatus.RETIRED, historical.getStatus());
    }

    @Test
    void inactiveVersionIsEditedInPlace() {
        CareServicePackage inactive = packageVersion(10L, 1, CareServicePackageStatus.INACTIVE);
        when(packageRepository.findById(10L)).thenReturn(Optional.of(inactive));
        when(packageRepository.save(inactive)).thenReturn(inactive);
        when(mapper.toCareServicePackageResponse(inactive))
                .thenReturn(CareServicePackageResponse.builder().id(10L).version(1).build());

        CareServicePackageResponse result = service.updatePackage(99L, UserRole.ADMIN, 10L, updateRequest());

        assertEquals(10L, result.getId());
        assertEquals(1, result.getVersion());
        verify(familyRepository, never()).findByIdForUpdate(any());
    }

    @Test
    void retiredVersionCannotBeEdited() {
        CareServicePackage retired = packageVersion(10L, 1, CareServicePackageStatus.RETIRED);
        when(packageRepository.findById(10L)).thenReturn(Optional.of(retired));

        assertThrows(
                AppException.class,
                () -> service.updatePackage(99L, UserRole.ADMIN, 10L, updateRequest())
        );
        verify(packageRepository, never()).save(any());
    }

    private CareServicePackage packageVersion(Long id, int version, CareServicePackageStatus status) {
        return CareServicePackage.builder()
                .id(id)
                .familyId(100L)
                .code("CARDIO")
                .versionNumber(version)
                .name("Cardio care")
                .shortDescription("Remote follow-up")
                .description("Detailed care description")
                .priceAmount(new BigDecimal("990000.00"))
                .currency("VND")
                .durationDays(7)
                .includedServices(List.of(
                        CareServiceCode.SECURE_MESSAGING,
                        CareServiceCode.HEALTH_RECORD_REVIEW
                ))
                .excludedServices(List.of(CareServiceCode.EMERGENCY_CARE))
                .requiredSpecialty(DoctorSpecialty.CARDIOLOGY)
                .supportPolicy(CareServiceSupportPolicy.ASSIGNED_DOCTOR_SUPPORT_SCHEDULE)
                .renewable(true)
                .termsPolicyReference("terms:v1")
                .status(status)
                .build();
    }

    private UpdateCareServicePackageRequest updateRequest() {
        UpdateCareServicePackageRequest request = new UpdateCareServicePackageRequest();
        request.setName("Cardio care plus");
        request.setDetailedDescription("Updated detailed care description");
        request.setPriceAmount(new BigDecimal("1290000.00"));
        request.setCurrency("VND");
        request.setDurationDays(14);
        request.setIncludedServices(List.of(
                CareServiceCode.SECURE_MESSAGING,
                CareServiceCode.HEALTH_RECORD_REVIEW,
                CareServiceCode.FINAL_CARE_SUMMARY
        ));
        request.setExcludedServices(List.of(
                CareServiceCode.EMERGENCY_CARE,
                CareServiceCode.TWENTY_FOUR_SEVEN_SUPPORT
        ));
        request.setRequiredSpecialty(DoctorSpecialty.CARDIOLOGY);
        request.setSupportPolicy(CareServiceSupportPolicy.ASSIGNED_DOCTOR_SUPPORT_SCHEDULE);
        request.setRenewable(true);
        request.setTermsPolicyReference("terms:v2");
        return request;
    }
}
