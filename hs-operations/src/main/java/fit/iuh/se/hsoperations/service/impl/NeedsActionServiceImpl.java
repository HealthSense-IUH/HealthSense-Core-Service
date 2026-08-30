package fit.iuh.se.hsoperations.service.impl;

import fit.iuh.se.hsoperations.dto.response.NeedsActionResponse;
import fit.iuh.se.hsoperations.entity.NeedsActionItem;
import fit.iuh.se.hsoperations.entity.enums.*;
import fit.iuh.se.hsoperations.repository.NeedsActionItemRepository;
import fit.iuh.se.hsoperations.service.NeedsActionService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class NeedsActionServiceImpl implements NeedsActionService {
    private final NeedsActionItemRepository repository;

    @Override @Transactional(readOnly = true)
    public PageResponse<NeedsActionResponse> find(UserRole role, NeedsActionStatus status, NeedsActionType type, Pageable pageable) {
        Specification<NeedsActionItem> spec = allowed(role);
        if (status != null) spec = spec.and((r, q, b) -> b.equal(r.get("status"), status));
        if (type != null) spec = spec.and((r, q, b) -> b.equal(r.get("type"), type));
        return new PageResponse<>(repository.findAll(spec, pageable).map(NeedsActionResponse::from));
    }

    @Override @Transactional(readOnly = true)
    public NeedsActionResponse get(UserRole role, Long id) {
        return repository.findOne(allowed(role).and((r, q, b) -> b.equal(r.get("id"), id)))
                .map(NeedsActionResponse::from).orElseThrow(this::notFound);
    }

    @Override @Transactional
    public NeedsActionResponse claim(UserRole role, Long actorId, Long id) {
        NeedsActionItem item = lockedAllowed(role, id);
        if (item.getStatus() == NeedsActionStatus.RESOLVED) throw new AppException(ErrorCode.BAD_REQUEST, "Needs Action is resolved");
        if (item.getStatus() == NeedsActionStatus.CLAIMED && !actorId.equals(item.getClaimedBy()))
            throw new AppException(ErrorCode.DATA_INTEGRITY_VIOLATION, "Needs Action is already claimed");
        item.setStatus(NeedsActionStatus.CLAIMED);
        item.setClaimedBy(actorId);
        item.setClaimedAt(Instant.now());
        return NeedsActionResponse.from(repository.save(item));
    }

    @Override @Transactional
    public NeedsActionResponse resolve(UserRole role, Long actorId, Long id, String resolution) {
        NeedsActionItem item = lockedAllowed(role, id);
        if (item.getStatus() == NeedsActionStatus.RESOLVED) return NeedsActionResponse.from(item);
        item.setStatus(NeedsActionStatus.RESOLVED);
        item.setResolvedBy(actorId);
        item.setResolvedAt(Instant.now());
        item.setResolution(resolution.strip());
        return NeedsActionResponse.from(repository.save(item));
    }

    private NeedsActionItem lockedAllowed(UserRole role, Long id) {
        NeedsActionItem item = repository.findLockedById(id).orElseThrow(this::notFound);
        if (!canAccess(role, item)) throw notFound();
        return item;
    }

    private Specification<NeedsActionItem> allowed(UserRole role) {
        if (role == UserRole.SUPER_ADMIN) return Specification.allOf();
        if (role == UserRole.ADMIN) return (r, q, b) -> r.get("assignedRole").in("ADMIN", "SUPER_ADMIN");
        if (role == UserRole.CARE_COORDINATOR) return (r, q, b) -> b.equal(r.get("assignedRole"), "CARE_COORDINATOR");
        throw new AppException(ErrorCode.ACCESS_DENIED);
    }

    private boolean canAccess(UserRole role, NeedsActionItem item) {
        return role == UserRole.SUPER_ADMIN
                || role == UserRole.ADMIN && (item.getAssignedRole().equals("ADMIN") || item.getAssignedRole().equals("SUPER_ADMIN"))
                || role == UserRole.CARE_COORDINATOR && item.getAssignedRole().equals("CARE_COORDINATOR");
    }

    private AppException notFound() { return new AppException(ErrorCode.ENTITY_NOT_FOUND, "Needs Action not found"); }
}
