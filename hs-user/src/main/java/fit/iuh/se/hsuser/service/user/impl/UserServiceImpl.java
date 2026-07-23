package fit.iuh.se.hsuser.service.user.impl;

import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.utils.TextNormalize;
import fit.iuh.se.hsuser.dto.request.UserProfileUpdateRequest;
import fit.iuh.se.hsuser.dto.response.UserResponse;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.UserProfile;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsuser.mapper.UserMapper;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import fit.iuh.se.hsuser.service.user.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
    @Transactional
    public UserResponse updateProfile(Long currentUserId, UserRole currentUserRole, UserProfileUpdateRequest request) {
        if (currentUserId == null)
            throw new AppException(ErrorCode.INVALID_ARGUMENT, "Current user ID must not be null");
        if (currentUserRole == null)
            throw new AppException(ErrorCode.INVALID_ARGUMENT, "Current user role must not be null");
        if (request == null)
            throw new AppException(ErrorCode.INVALID_ARGUMENT, "User profile update request must not be null");
        validateSelfUpdateRole(currentUserRole);

        UserAccount user = userAccountRepository.findByIdAndStatusNot(currentUserId, AccountStatus.INACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        updateProfile(user.getProfile(), request);
        user.setUpdatedAt(Instant.now());
        return userMapper.toUserResponse(userAccountRepository.save(user));
    }

    private void validateSelfUpdateRole(UserRole currentUserRole) {
        if (currentUserRole == UserRole.DOCTOR || currentUserRole == UserRole.MEMBER)
            return;
        throw new AppException(ErrorCode.ACCESS_DENIED, "Only doctors and members can update profile here");
    }

    private void updateProfile(UserProfile profile, UserProfileUpdateRequest request) {
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
