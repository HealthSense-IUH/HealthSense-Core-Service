package fit.iuh.se.hsoperations.service;

import fit.iuh.se.hsoperations.dto.response.NeedsActionResponse;
import fit.iuh.se.hsoperations.entity.enums.*;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import org.springframework.data.domain.Pageable;

public interface NeedsActionService {
    PageResponse<NeedsActionResponse> find(UserRole role, NeedsActionStatus status, NeedsActionType type, Pageable pageable);
    NeedsActionResponse get(UserRole role, Long id);
    NeedsActionResponse claim(UserRole role, Long actorId, Long id);
    NeedsActionResponse resolve(UserRole role, Long actorId, Long id, String resolution);
}
