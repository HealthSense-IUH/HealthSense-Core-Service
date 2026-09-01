package fit.iuh.se.hschat.service.packageinfo.impl;

import fit.iuh.se.hschat.dto.request.CreateCareServicePackageRequest;
import fit.iuh.se.hschat.dto.request.UpdateCareServicePackageRequest;
import fit.iuh.se.hschat.dto.response.CareServicePackageResponse;
import fit.iuh.se.hschat.entity.CareServicePackage;
import fit.iuh.se.hschat.entity.CareServicePackageFamily;
import fit.iuh.se.hschat.entity.enums.CareServiceCode;
import fit.iuh.se.hschat.entity.enums.CareServicePackageStatus;
import fit.iuh.se.hschat.entity.enums.CareServiceSupportPolicy;
import fit.iuh.se.hschat.mapper.ConsultationMapper;
import fit.iuh.se.hschat.repository.CareServicePackageFamilyRepository;
import fit.iuh.se.hschat.repository.CareServicePackageRepository;
import fit.iuh.se.hschat.service.packageinfo.CareServicePackageService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsoperations.dto.command.OperationalEventCommand;
import fit.iuh.se.hsoperations.entity.enums.*;
import fit.iuh.se.hsoperations.event.OperationalEventPublisher;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CareServicePackageServiceImpl implements CareServicePackageService {

    static String DEFAULT_CURRENCY = "VND";
    static CareServiceSupportPolicy DEFAULT_SUPPORT_POLICY =
            CareServiceSupportPolicy.ASSIGNED_DOCTOR_SUPPORT_SCHEDULE;
    static List<CareServiceCode> DEFAULT_INCLUDED_SERVICES =
            List.of(CareServiceCode.REMOTE_ONE_ON_ONE_CARE);
    static List<CareServiceCode> DEFAULT_EXCLUDED_SERVICES =
            List.of(CareServiceCode.EMERGENCY_CARE);

    CareServicePackageRepository packageRepository;
    CareServicePackageFamilyRepository familyRepository;
    ConsultationMapper mapper;
    OperationalEventPublisher OperationalEventPublisher;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CareServicePackageResponse> getActivePackages(Pageable pageable) {
        Page<CareServicePackageResponse> page = packageRepository
                .findByStatusOrderByCreatedAtDesc(CareServicePackageStatus.ACTIVE, pageable)
                .map(mapper::toCareServicePackageResponse);
        return new PageResponse<>(page);
    }

    @Override
    @Transactional(readOnly = true)
    public CareServicePackageResponse getActivePackage(Long packageId) {
        return packageRepository.findByIdAndStatus(packageId, CareServicePackageStatus.ACTIVE)
                .map(mapper::toCareServicePackageResponse)
                .orElseThrow(() -> new AppException(ErrorCode.CARE_SERVICE_PACKAGE_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CareServicePackageResponse> getPackagesForAdmin(
            UserRole actorRole,
            CareServicePackageStatus status,
            Pageable pageable
    ) {
        validatePackageManager(actorRole);
        Page<CareServicePackage> packages = status == null
                ? packageRepository.findAll(pageable)
                : packageRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        return new PageResponse<>(packages.map(mapper::toCareServicePackageResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public CareServicePackageResponse getPackageForAdmin(UserRole actorRole, Long packageId) {
        validatePackageManager(actorRole);
        return mapper.toCareServicePackageResponse(findPackage(packageId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CareServicePackageResponse> getPackageVersionsForAdmin(UserRole actorRole, Long packageId) {
        validatePackageManager(actorRole);
        CareServicePackage carePackage = findPackage(packageId);
        return packageRepository.findByFamilyIdOrderByVersionNumberDesc(carePackage.getFamilyId())
                .stream()
                .map(mapper::toCareServicePackageResponse)
                .toList();
    }

    @Override
    @Transactional
    public CareServicePackageResponse createPackage(
            Long actorId,
            UserRole actorRole,
            CreateCareServicePackageRequest request
    ) {
        validatePackageManager(actorRole);
        String code = normalizeCode(request.getCode());
        if (familyRepository.existsByCode(code))
            throw new AppException(ErrorCode.CARE_SERVICE_PACKAGE_CODE_ALREADY_EXISTS);

        CareServicePackageFamily family = familyRepository.saveAndFlush(
                CareServicePackageFamily.builder().code(code).build()
        );
        CareServicePackage carePackage = CareServicePackage.builder()
                .familyId(family.getId())
                .code(code)
                .versionNumber(1)
                .name(request.getName().trim())
                .shortDescription(trimToNull(request.getShortDescription()))
                .description(resolveCreateDescription(request))
                .priceAmount(request.getPriceAmount())
                .currency(normalizeCurrency(request.getCurrency(), DEFAULT_CURRENCY))
                .durationDays(request.getDurationDays())
                .includedServices(normalizeServices(request.getIncludedServices(), DEFAULT_INCLUDED_SERVICES))
                .excludedServices(normalizeServices(request.getExcludedServices(), DEFAULT_EXCLUDED_SERVICES))
                .requiredSpecialty(request.getRequiredSpecialty())
                .supportPolicy(request.getSupportPolicy() == null ? DEFAULT_SUPPORT_POLICY : request.getSupportPolicy())
                .renewable(request.getRenewable())
                .termsPolicyReference(trimToNull(request.getTermsPolicyReference()))
                .status(CareServicePackageStatus.INACTIVE)
                .build();
        validateServiceScope(carePackage.getIncludedServices(), carePackage.getExcludedServices());

        carePackage = saveVersion(carePackage);
        auditPackage(carePackage, BusinessEventType.PACKAGE_CREATED, actorId, actorRole, null, carePackage.getStatus());
        return mapper.toCareServicePackageResponse(carePackage);
    }

    @Override
    @Transactional
    public CareServicePackageResponse updatePackage(
            Long actorId,
            UserRole actorRole,
            Long packageId,
            UpdateCareServicePackageRequest request
    ) {
        validatePackageManager(actorRole);
        CareServicePackage carePackage = findPackage(packageId);
        ensureNotRetired(carePackage);

        if (carePackage.getStatus() == CareServicePackageStatus.INACTIVE) {
            applyUpdate(carePackage, request);
            carePackage = packageRepository.save(carePackage);
            auditPackage(carePackage, BusinessEventType.PACKAGE_UPDATED, actorId, actorRole,
                    CareServicePackageStatus.INACTIVE, carePackage.getStatus());
            return mapper.toCareServicePackageResponse(carePackage);
        }

        lockFamily(carePackage.getFamilyId());
        CareServicePackage publishedVersion = findPackage(packageId);
        if (publishedVersion.getStatus() != CareServicePackageStatus.ACTIVE)
            throw new AppException(ErrorCode.INVALID_CARE_SERVICE_PACKAGE_STATUS);

        int nextVersion = packageRepository.findTopByFamilyIdOrderByVersionNumberDesc(publishedVersion.getFamilyId())
                .map(version -> version.getVersionNumber() + 1)
                .orElse(1);
        CareServicePackage newVersion = copyForNextVersion(publishedVersion, nextVersion);
        applyUpdate(newVersion, request);

        publishedVersion.setStatus(CareServicePackageStatus.RETIRED);
        packageRepository.saveAndFlush(publishedVersion);
        newVersion = saveVersion(newVersion);
        auditPackage(publishedVersion, BusinessEventType.PACKAGE_RETIRED, actorId, actorRole,
                CareServicePackageStatus.ACTIVE, CareServicePackageStatus.RETIRED);
        auditPackage(newVersion, BusinessEventType.PACKAGE_UPDATED, actorId, actorRole, null, newVersion.getStatus());
        return mapper.toCareServicePackageResponse(newVersion);
    }

    @Override
    @Transactional
    public CareServicePackageResponse activatePackage(Long actorId, UserRole actorRole, Long packageId) {
        validatePackageManager(actorRole);
        CareServicePackage carePackage = findPackage(packageId);
        ensureNotRetired(carePackage);
        if (carePackage.getStatus() == CareServicePackageStatus.ACTIVE)
            return mapper.toCareServicePackageResponse(carePackage);

        lockFamily(carePackage.getFamilyId());
        packageRepository.findByFamilyIdAndStatus(carePackage.getFamilyId(), CareServicePackageStatus.ACTIVE)
                .ifPresent(active -> {
                    active.setStatus(CareServicePackageStatus.RETIRED);
                    packageRepository.saveAndFlush(active);
                });
        carePackage.setStatus(CareServicePackageStatus.ACTIVE);
        carePackage = packageRepository.save(carePackage);
        auditPackage(carePackage, BusinessEventType.PACKAGE_ACTIVATED, actorId, actorRole,
                CareServicePackageStatus.INACTIVE, CareServicePackageStatus.ACTIVE);
        return mapper.toCareServicePackageResponse(carePackage);
    }

    @Override
    @Transactional
    public CareServicePackageResponse deactivatePackage(Long actorId, UserRole actorRole, Long packageId) {
        validatePackageManager(actorRole);
        CareServicePackage carePackage = findPackage(packageId);
        ensureNotRetired(carePackage);
        if (carePackage.getStatus() == CareServicePackageStatus.ACTIVE)
            carePackage.setStatus(CareServicePackageStatus.RETIRED);
        carePackage = packageRepository.save(carePackage);
        auditPackage(carePackage, BusinessEventType.PACKAGE_DEACTIVATED, actorId, actorRole,
                CareServicePackageStatus.ACTIVE, carePackage.getStatus());
        return mapper.toCareServicePackageResponse(carePackage);
    }

    @Override
    @Transactional
    public CareServicePackageResponse retirePackage(Long actorId, UserRole actorRole, Long packageId) {
        validatePackageManager(actorRole);
        CareServicePackage carePackage = findPackage(packageId);
        carePackage.setStatus(CareServicePackageStatus.RETIRED);
        carePackage = packageRepository.save(carePackage);
        auditPackage(carePackage, BusinessEventType.PACKAGE_RETIRED, actorId, actorRole,
                null, CareServicePackageStatus.RETIRED);
        return mapper.toCareServicePackageResponse(carePackage);
    }

    private void auditPackage(CareServicePackage carePackage, BusinessEventType eventType, Long actorId, UserRole actorRole,
            CareServicePackageStatus previous, CareServicePackageStatus next) {
        OperationalEventPublisher.record(OperationalEventCommand.builder()
                .domainType(BusinessDomainType.PACKAGE).domainId(carePackage.getId()).eventType(eventType)
                .actorType(BusinessActorType.USER).actorUserId(actorId).actorRole(actorRole.name())
                .previousState(previous == null ? null : previous.name()).newState(next == null ? null : next.name())
                .metadata(java.util.Map.of("familyId", String.valueOf(carePackage.getFamilyId()),
                        "version", String.valueOf(carePackage.getVersionNumber())))
                .idempotencyKey("package:" + carePackage.getId() + ":" + eventType + ":" + carePackage.getVersionNumber())
                .build());
    }

    private CareServicePackage copyForNextVersion(CareServicePackage source, int nextVersion) {
        return CareServicePackage.builder()
                .familyId(source.getFamilyId())
                .code(source.getCode())
                .versionNumber(nextVersion)
                .name(source.getName())
                .shortDescription(source.getShortDescription())
                .description(source.getDescription())
                .priceAmount(source.getPriceAmount())
                .currency(source.getCurrency())
                .durationDays(source.getDurationDays())
                .includedServices(new ArrayList<>(source.getIncludedServices()))
                .excludedServices(new ArrayList<>(source.getExcludedServices()))
                .requiredSpecialty(source.getRequiredSpecialty())
                .supportPolicy(source.getSupportPolicy())
                .renewable(source.getRenewable())
                .termsPolicyReference(source.getTermsPolicyReference())
                .status(CareServicePackageStatus.ACTIVE)
                .build();
    }

    private void applyUpdate(CareServicePackage carePackage, UpdateCareServicePackageRequest request) {
        carePackage.setName(request.getName().trim());
        carePackage.setShortDescription(request.getShortDescription() == null
                ? carePackage.getShortDescription()
                : trimToNull(request.getShortDescription()));
        if (request.getDetailedDescription() != null)
            carePackage.setDescription(trimToNull(request.getDetailedDescription()));
        else if (request.getDescription() != null)
            carePackage.setDescription(trimToNull(request.getDescription()));
        carePackage.setPriceAmount(request.getPriceAmount());
        carePackage.setCurrency(normalizeCurrency(request.getCurrency(), carePackage.getCurrency()));
        carePackage.setDurationDays(request.getDurationDays());
        if (request.getIncludedServices() != null)
            carePackage.setIncludedServices(normalizeServices(request.getIncludedServices(), List.of()));
        if (request.getExcludedServices() != null)
            carePackage.setExcludedServices(normalizeServices(request.getExcludedServices(), List.of()));
        if (request.getRequiredSpecialty() != null)
            carePackage.setRequiredSpecialty(request.getRequiredSpecialty());
        if (request.getSupportPolicy() != null)
            carePackage.setSupportPolicy(request.getSupportPolicy());
        carePackage.setRenewable(request.getRenewable());
        if (request.getTermsPolicyReference() != null)
            carePackage.setTermsPolicyReference(trimToNull(request.getTermsPolicyReference()));
        validateServiceScope(carePackage.getIncludedServices(), carePackage.getExcludedServices());
    }

    private CareServicePackage saveVersion(CareServicePackage carePackage) {
        try {
            return packageRepository.saveAndFlush(carePackage);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(
                    ErrorCode.DATA_INTEGRITY_VIOLATION,
                    "Package version identity conflicts with an existing version"
            );
        }
    }

    private void lockFamily(Long familyId) {
        familyRepository.findByIdForUpdate(familyId)
                .orElseThrow(() -> new AppException(ErrorCode.CARE_SERVICE_PACKAGE_NOT_FOUND));
    }

    private CareServicePackage findPackage(Long packageId) {
        return packageRepository.findById(packageId)
                .orElseThrow(() -> new AppException(ErrorCode.CARE_SERVICE_PACKAGE_NOT_FOUND));
    }

    private void validatePackageManager(UserRole actorRole) {
        if (actorRole == UserRole.SUPER_ADMIN || actorRole == UserRole.ADMIN)
            return;
        throw new AppException(ErrorCode.ACCESS_DENIED, "You are not allowed to manage care service packages");
    }

    private void ensureNotRetired(CareServicePackage carePackage) {
        if (carePackage.getStatus() == CareServicePackageStatus.RETIRED)
            throw new AppException(ErrorCode.INVALID_CARE_SERVICE_PACKAGE_STATUS);
    }

    private void validateServiceScope(List<CareServiceCode> included, List<CareServiceCode> excluded) {
        if (included == null || included.isEmpty())
            throw new AppException(ErrorCode.BAD_REQUEST, "At least one included service is required");
        Set<CareServiceCode> overlap = new LinkedHashSet<>(included);
        overlap.retainAll(excluded == null ? List.of() : excluded);
        if (!overlap.isEmpty())
            throw new AppException(ErrorCode.BAD_REQUEST, "A service cannot be both included and excluded");
    }

    private List<CareServiceCode> normalizeServices(
            List<CareServiceCode> services,
            List<CareServiceCode> defaults
    ) {
        List<CareServiceCode> source = services == null ? defaults : services;
        if (source.stream().anyMatch(Objects::isNull))
            throw new AppException(ErrorCode.BAD_REQUEST, "Service scope contains an invalid value");
        return new ArrayList<>(new LinkedHashSet<>(source));
    }

    private String resolveCreateDescription(CreateCareServicePackageRequest request) {
        return request.getDetailedDescription() == null
                ? trimToNull(request.getDescription())
                : trimToNull(request.getDetailedDescription());
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeCurrency(String currency, String fallback) {
        return currency == null ? fallback : currency.trim().toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null)
            return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
