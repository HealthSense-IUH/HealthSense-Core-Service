package fit.iuh.se.hsuser.service.admin.impl;

import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsshared.utils.TextNormalize;
import fit.iuh.se.hsuser.dto.request.AdminUserCreateRequest;
import fit.iuh.se.hsuser.dto.request.UserUpdateRequest;
import fit.iuh.se.hsuser.dto.response.AdminUserCreateResult;
import fit.iuh.se.hsuser.dto.response.UserResponse;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.UserProfile;
import fit.iuh.se.hsuser.entity.UserSensitiveData;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsuser.mapper.UserMapper;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import fit.iuh.se.hsuser.service.admin.AdminUserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;

/**
 * @author : user664dntp
 * @mailto : phatdang19052004@gmail.com
 * @created : 23/07/2026, Thursday
 **/

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminUserServiceImpl implements AdminUserService {

    private static final String PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%";
    private static final int TEMPORARY_PASSWORD_LENGTH = 12;

    UserAccountRepository userAccountRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AdminUserCreateResult createUser(Long currentUserId, UserRole currentUserRole, AdminUserCreateRequest request) {
        if (currentUserId == null)
            throw new AppException(ErrorCode.INVALID_ARGUMENT, "Current user ID must not be null");
        if (currentUserRole == null)
            throw new AppException(ErrorCode.INVALID_ARGUMENT, "Current user role must not be null");
        if (request == null)
            throw new AppException(ErrorCode.INVALID_ARGUMENT, "User create request must not be null");
        validateRoleVisibility(currentUserRole, request.getRole());

        String email = TextNormalize.normalizeEmail(
                TextNormalize.requireText(request.getEmail(), "Email must not be blank")
        );
        if (userAccountRepository.existsByEmail(email))
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);

        String temporaryPassword = generateTemporaryPassword();
        UserAccount user = UserAccount.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(temporaryPassword))
                .role(request.getRole())
                .status(AccountStatus.ACTIVE)
                .build();

        UserProfile profile = UserProfile.builder()
                .user(user)
                .displayName(TextNormalize.requireText(request.getDisplayName(), "Display name must not be blank"))
                .phone(trimToNull(request.getPhone()))
                .dateOfBirth(request.getDateOfBirth())
                .gender(trimToNull(request.getGender()))
                .address(trimToNull(request.getAddress()))
                .build();

        UserSensitiveData sensitiveData = UserSensitiveData.builder()
                .user(user)
                .build();

        user.setProfile(profile);
        user.setSensitiveData(sensitiveData);

        UserResponse response = userMapper.toUserResponse(userAccountRepository.save(user));
        return AdminUserCreateResult.builder()
                .user(response)
                .temporaryPassword(temporaryPassword)
                .build();
    }

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
    @Transactional(readOnly = true)
    public UserResponse getUser(Long currentUserId, UserRole currentUserRole, Long id) {
        UserAccount user = findManageableUser(currentUserId, currentUserRole, id);
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long currentUserId, UserRole currentUserRole, Long id, UserUpdateRequest request) {
        if (request == null)
            throw new AppException(ErrorCode.INVALID_ARGUMENT, "User update request must not be null");
        if (request.getRole() != null)
            throw new AppException(ErrorCode.ACCESS_DENIED, "Role cannot be updated");
        UserAccount user = findManageableUser(currentUserId, currentUserRole, id);
        updateAccount(user, request);
        updateProfile(user.getProfile(), request);
        user.setUpdatedAt(Instant.now());
        return userMapper.toUserResponse(userAccountRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long currentUserId, UserRole currentUserRole, Long id) {
        UserAccount user = findManageableUser(currentUserId, currentUserRole, id);
        user.setStatus(AccountStatus.INACTIVE);
        user.setUpdatedAt(Instant.now());
        userAccountRepository.save(user);
    }

    private UserAccount findAvailableUser(Long id) {
        if (id == null)
            throw new AppException(ErrorCode.INVALID_ARGUMENT, "User ID must not be null");
        return userAccountRepository.findByIdAndStatusNot(id, AccountStatus.INACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private UserAccount findManageableUser(Long currentUserId, UserRole currentUserRole, Long targetUserId) {
        if (currentUserId == null)
            throw new AppException(ErrorCode.INVALID_ARGUMENT, "Current user ID must not be null");
        if (currentUserRole == null)
            throw new AppException(ErrorCode.INVALID_ARGUMENT, "Current user role must not be null");
        UserAccount targetUser = findAvailableUser(targetUserId);
        if (targetUser.getId().equals(currentUserId))
            throw new AppException(ErrorCode.ACCESS_DENIED, "You are not allowed to manage your own account");
        validateRoleVisibility(currentUserRole, targetUser.getRole());
        return targetUser;
    }

    private void validateRoleVisibility(UserRole currentUserRole, UserRole targetRole) {
        if (currentUserRole == UserRole.SUPER_ADMIN && targetRole != UserRole.SUPER_ADMIN)
            return;
        if (currentUserRole == UserRole.ADMIN && (targetRole == UserRole.DOCTOR || targetRole == UserRole.MEMBER))
            return;
        throw new AppException(ErrorCode.ACCESS_DENIED, "You are not allowed to view users with this role");
    }

    private String generateTemporaryPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(TEMPORARY_PASSWORD_LENGTH);
        for (int i = 0; i < TEMPORARY_PASSWORD_LENGTH; i++) {
            password.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
        }
        return password.toString();
    }

    private String trimToNull(String value) {
        if (value == null)
            return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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
