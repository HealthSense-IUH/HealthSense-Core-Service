package fit.iuh.se.hsuser.service.admin;

import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsuser.dto.request.AdminUserCreateRequest;
import fit.iuh.se.hsuser.dto.request.UserUpdateRequest;
import fit.iuh.se.hsuser.dto.response.AdminUserCreateResult;
import fit.iuh.se.hsuser.dto.response.UserResponse;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import org.springframework.data.domain.Pageable;

/**
 * @author : user664dntp
 * @mailto : phatdang19052004@gmail.com
 * @created : 23/07/2026, Thursday
 **/
public interface AdminUserService {

    AdminUserCreateResult createUser(Long currentUserId, UserRole currentUserRole, AdminUserCreateRequest request);

    PageResponse<UserResponse> getUsers(Long currentUserId, UserRole currentUserRole, UserRole targetRole, Pageable pageable);

    UserResponse getUser(Long id);

    UserResponse getUser(Long currentUserId, UserRole currentUserRole, Long id);

    UserResponse updateUser(Long currentUserId, UserRole currentUserRole, Long id, UserUpdateRequest request);

    void deleteUser(Long currentUserId, UserRole currentUserRole, Long id);
}
