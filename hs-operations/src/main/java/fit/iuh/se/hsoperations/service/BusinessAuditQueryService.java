package fit.iuh.se.hsoperations.service;

import fit.iuh.se.hsoperations.dto.response.BusinessAuditEventResponse;
import fit.iuh.se.hsoperations.entity.enums.*;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import org.springframework.data.domain.Pageable;

public interface BusinessAuditQueryService {
    PageResponse<BusinessAuditEventResponse> find(UserRole role, BusinessDomainType domainType, Long domainId,
            BusinessEventType eventType, Pageable pageable);
    BusinessAuditEventResponse get(UserRole role, Long id);
}
