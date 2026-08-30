package fit.iuh.se.hschat.service.packageinfo;

import fit.iuh.se.hschat.dto.request.CreateCareServicePackageRequest;
import fit.iuh.se.hschat.dto.request.UpdateCareServicePackageRequest;
import fit.iuh.se.hschat.dto.response.CareServicePackageResponse;
import fit.iuh.se.hschat.entity.enums.CareServicePackageStatus;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CareServicePackageService {

    PageResponse<CareServicePackageResponse> getActivePackages(Pageable pageable);

    CareServicePackageResponse getActivePackage(Long packageId);

    PageResponse<CareServicePackageResponse> getPackagesForAdmin(
            UserRole actorRole,
            CareServicePackageStatus status,
            Pageable pageable
    );

    CareServicePackageResponse getPackageForAdmin(UserRole actorRole, Long packageId);

    List<CareServicePackageResponse> getPackageVersionsForAdmin(UserRole actorRole, Long packageId);

    CareServicePackageResponse createPackage(
            Long actorId,
            UserRole actorRole,
            CreateCareServicePackageRequest request
    );

    CareServicePackageResponse updatePackage(
            Long actorId,
            UserRole actorRole,
            Long packageId,
            UpdateCareServicePackageRequest request
    );

    CareServicePackageResponse activatePackage(Long actorId, UserRole actorRole, Long packageId);

    CareServicePackageResponse deactivatePackage(Long actorId, UserRole actorRole, Long packageId);

    CareServicePackageResponse retirePackage(Long actorId, UserRole actorRole, Long packageId);
}
