package fit.iuh.se.hsuser.service.admin.impl;

import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsshared.utils.TextNormalize;
import fit.iuh.se.hsuser.dto.request.UserUpdateRequest;
import fit.iuh.se.hsuser.dto.response.UserResponse;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.UserProfile;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsuser.mapper.UserMapper;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import fit.iuh.se.hsuser.service.admin.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * @author : user664dntp
 * @mailto : phatdang19052004@gmail.com
 * @created : 23/07/2026, Thursday
 **/

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserAccountRepository userAccountRepository;
    UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getUsers(Long currentUserId, UserRole currentUserRole, UserRole targetRole, Pageable pageable) {
        if (currentUserId == null)
            throw new AppException(ErrorCode.INVALID_ARGUMENT, "Current user ID must not be null");
        if (currentUserRole == null)
            throw new AppException(ErrorCode.INVALID_ARGUMENT, "Current user role must not be null");
        if (targetRole == null)
            throw new AppException(ErrorCode.INVALID_ARGUMENT, "Target role must not be null");
        if (pageable == null)
            throw new AppException(ErrorCode.INVALID_ARGUMENT, "Pageable must not be null");
        validateRoleVisibility(currentUserRole, targetRole);
        Page<UserResponse> users = userAccountRepository
                .findAllByStatusNotAndRoleAndIdNot(AccountStatus.INACTIVE, targetRole, currentUserId, pageable)
                .map(userMapper::toUserResponse);
        return new PageResponse<>(users);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        if (id == null)
            throw new AppException(ErrorCode.INVALID_ARGUMENT, "User ID must not be null");
        return userMapper.toUserResponse(findAvailableUser(id));
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        if (id == null)
            throw new AppException(ErrorCode.INVALID_ARGUMENT, "User ID must not be null");
        if (request == null)
            throw new AppException(ErrorCode.INVALID_ARGUMENT, "User update request must not be null");
        UserAccount user = findAvailableUser(id);
        updateAccount(user, request);
        updateProfile(user.getProfile(), request);
        user.setUpdatedAt(Instant.now());
        return userMapper.toUserResponse(userAccountRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (id == null)
            throw new AppException(ErrorCode.INVALID_ARGUMENT, "User ID must not be null");
        UserAccount user = findAvailableUser(id);
        user.setStatus(AccountStatus.INACTIVE);
        userAccountRepository.save(user);
    }

    private UserAccount findAvailableUser(Long id) {
        return userAccountRepository.findByIdAndStatusNot(id, AccountStatus.INACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private void validateRoleVisibility(UserRole currentUserRole, UserRole targetRole) {
        if (currentUserRole == UserRole.SUPER_ADMIN && targetRole != UserRole.SUPER_ADMIN)
            return;
        if (currentUserRole == UserRole.ADMIN
                && (targetRole == UserRole.DOCTOR || targetRole == UserRole.MEMBER))
            return;
        throw new AppException(ErrorCode.ACCESS_DENIED, "You are not allowed to view users with this role");
    }

    private void updateAccount(UserAccount user, UserUpdateRequest request) {
        if (request.getEmail() != null) {
            String email = TextNormalize.normalizeEmail(
                    TextNormalize.requireText(request.getEmail(), "Email must not be blank")
            );
            if (userAccountRepository.existsByEmailAndIdNot(email, user.getId()))
                throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
            user.setEmail(email);
        }
        if (request.getRole() != null)
            user.setRole(request.getRole());
        if (request.getStatus() != null)
            user.setStatus(request.getStatus());
    }

    private void updateProfile(UserProfile profile, UserUpdateRequest request) {
        if (request.getDisplayName() != null)
            profile.setDisplayName(TextNormalize.requireText(request.getDisplayName(), "Display name must not be blank"));
        if (request.getPhone() != null)
            profile.setPhone(request.getPhone().trim());
        if (request.getDateOfBirth() != null)
            profile.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null)
            profile.setGender(request.getGender().trim());
        if (request.getAddress() != null)
            profile.setAddress(request.getAddress().trim());
    }
}
