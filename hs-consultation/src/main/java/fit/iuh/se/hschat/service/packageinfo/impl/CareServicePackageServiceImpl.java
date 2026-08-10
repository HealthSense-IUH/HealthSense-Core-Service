package fit.iuh.se.hschat.service.packageinfo.impl;

import fit.iuh.se.hschat.dto.request.CreateCareServicePackageRequest;
import fit.iuh.se.hschat.dto.request.UpdateCareServicePackageRequest;
import fit.iuh.se.hschat.dto.response.CareServicePackageResponse;
import fit.iuh.se.hschat.entity.CareServicePackage;
import fit.iuh.se.hschat.entity.enums.CareServicePackageStatus;
import fit.iuh.se.hschat.mapper.ConsultationMapper;
import fit.iuh.se.hschat.repository.CareServicePackageRepository;
import fit.iuh.se.hschat.service.packageinfo.CareServicePackageService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CareServicePackageServiceImpl implements CareServicePackageService {

    static String DEFAULT_CURRENCY = "VND";

    CareServicePackageRepository packageRepository;
    ConsultationMapper mapper;

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
    @Transactional
    public CareServicePackageResponse createPackage(
            UserRole actorRole,
            CreateCareServicePackageRequest request
    ) {
        validatePackageManager(actorRole);
        String code = normalizeCode(request.getCode());
        if (packageRepository.existsByCode(code))
            throw new AppException(ErrorCode.CARE_SERVICE_PACKAGE_CODE_ALREADY_EXISTS);

        CareServicePackage carePackage = CareServicePackage.builder()
                .code(code)
                .name(request.getName().trim())
                .description(trimToNull(request.getDescription()))
                .priceAmount(request.getPriceAmount())
                .currency(DEFAULT_CURRENCY)
                .durationDays(request.getDurationDays())
                .renewable(request.getRenewable())
                .status(CareServicePackageStatus.INACTIVE)
                .build();

        return mapper.toCareServicePackageResponse(packageRepository.save(carePackage));
    }

    @Override
    @Transactional
    public CareServicePackageResponse updatePackage(
            UserRole actorRole,
            Long packageId,
            UpdateCareServicePackageRequest request
    ) {
        validatePackageManager(actorRole);
        CareServicePackage carePackage = findPackage(packageId);
        ensureNotRetired(carePackage);

        carePackage.setName(request.getName().trim());
        carePackage.setDescription(trimToNull(request.getDescription()));
        carePackage.setPriceAmount(request.getPriceAmount());
        carePackage.setDurationDays(request.getDurationDays());
        carePackage.setRenewable(request.getRenewable());

        return mapper.toCareServicePackageResponse(packageRepository.save(carePackage));
    }

    @Override
    @Transactional
    public CareServicePackageResponse activatePackage(UserRole actorRole, Long packageId) {
        validatePackageManager(actorRole);
        CareServicePackage carePackage = findPackage(packageId);
        ensureNotRetired(carePackage);
        carePackage.setStatus(CareServicePackageStatus.ACTIVE);
        return mapper.toCareServicePackageResponse(packageRepository.save(carePackage));
    }

    @Override
    @Transactional
    public CareServicePackageResponse deactivatePackage(UserRole actorRole, Long packageId) {
        validatePackageManager(actorRole);
        CareServicePackage carePackage = findPackage(packageId);
        ensureNotRetired(carePackage);
        carePackage.setStatus(CareServicePackageStatus.INACTIVE);
        return mapper.toCareServicePackageResponse(packageRepository.save(carePackage));
    }

    @Override
    @Transactional
    public CareServicePackageResponse retirePackage(UserRole actorRole, Long packageId) {
        validatePackageManager(actorRole);
        CareServicePackage carePackage = findPackage(packageId);
        carePackage.setStatus(CareServicePackageStatus.RETIRED);
        return mapper.toCareServicePackageResponse(packageRepository.save(carePackage));
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

    private String normalizeCode(String code) {
        return code.trim().toUpperCase();
    }

    private String trimToNull(String value) {
        if (value == null)
            return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
