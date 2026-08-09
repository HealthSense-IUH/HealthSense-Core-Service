package fit.iuh.se.hschat.service.packageinfo;

import fit.iuh.se.hschat.dto.response.CareServicePackageResponse;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface CareServicePackageService {

    PageResponse<CareServicePackageResponse> getActivePackages(Pageable pageable);

    CareServicePackageResponse getActivePackage(Long packageId);
}
