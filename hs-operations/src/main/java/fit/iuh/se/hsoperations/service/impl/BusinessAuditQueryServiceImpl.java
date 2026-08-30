package fit.iuh.se.hsoperations.service.impl;

import fit.iuh.se.hsoperations.dto.response.BusinessAuditEventResponse;
import fit.iuh.se.hsoperations.entity.BusinessAuditEvent;
import fit.iuh.se.hsoperations.entity.enums.*;
import fit.iuh.se.hsoperations.repository.BusinessAuditEventRepository;
import fit.iuh.se.hsoperations.service.BusinessAuditQueryService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BusinessAuditQueryServiceImpl implements BusinessAuditQueryService {
    private final BusinessAuditEventRepository repository;

    @Override @Transactional(readOnly = true)
    public PageResponse<BusinessAuditEventResponse> find(UserRole role, BusinessDomainType domainType, Long domainId,
            BusinessEventType eventType, Pageable pageable) {
        Specification<BusinessAuditEvent> spec = allowed(role);
        if (domainType != null) spec = spec.and((r, q, b) -> b.equal(r.get("domainType"), domainType));
        if (domainId != null) spec = spec.and((r, q, b) -> b.equal(r.get("domainId"), domainId));
        if (eventType != null) spec = spec.and((r, q, b) -> b.equal(r.get("eventType"), eventType));
        return new PageResponse<>(repository.findAll(spec, pageable).map(BusinessAuditEventResponse::from));
    }

    @Override @Transactional(readOnly = true)
    public BusinessAuditEventResponse get(UserRole role, Long id) {
        return repository.findOne(allowed(role).and((r, q, b) -> b.equal(r.get("id"), id)))
                .map(BusinessAuditEventResponse::from)
                .orElseThrow(() -> new AppException(ErrorCode.ENTITY_NOT_FOUND, "Business audit event not found"));
    }

    private Specification<BusinessAuditEvent> allowed(UserRole role) {
        if (role == UserRole.ADMIN || role == UserRole.SUPER_ADMIN) return Specification.allOf();
        if (role == UserRole.CARE_COORDINATOR) {
            return (r, q, b) -> r.get("domainType").in(BusinessDomainType.REQUEST, BusinessDomainType.RESERVATION,
                    BusinessDomainType.AGREEMENT, BusinessDomainType.PAYMENT, BusinessDomainType.SESSION,
                    BusinessDomainType.RENEWAL, BusinessDomainType.REFUND, BusinessDomainType.FINAL_SUMMARY);
        }
        throw new AppException(ErrorCode.ACCESS_DENIED);
    }
}
