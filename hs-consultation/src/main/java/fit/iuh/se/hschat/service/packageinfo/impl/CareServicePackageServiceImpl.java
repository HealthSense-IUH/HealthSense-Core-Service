package fit.iuh.se.hschat.service.packageinfo.impl;

import fit.iuh.se.hschat.dto.response.CareServicePackageResponse;
import fit.iuh.se.hschat.entity.enums.CareServicePackageStatus;
import fit.iuh.se.hschat.mapper.ConsultationMapper;
import fit.iuh.se.hschat.repository.CareServicePackageRepository;
import fit.iuh.se.hschat.service.packageinfo.CareServicePackageService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.PageResponse;
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
}
