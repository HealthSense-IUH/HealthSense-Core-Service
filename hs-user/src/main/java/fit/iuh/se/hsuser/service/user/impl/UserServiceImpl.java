package fit.iuh.se.hsuser.service.user.impl;

import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.service.s3.S3Service;
import fit.iuh.se.hsshared.utils.TextNormalize;
import fit.iuh.se.hsuser.dto.request.AvatarPresignedUrlRequest;
import fit.iuh.se.hsuser.dto.request.UserProfileUpdateRequest;
import fit.iuh.se.hsuser.dto.response.AvatarPresignedUrlResponse;
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
import lombok.extern.slf4j.Slf4j;
import fit.iuh.se.hsshared.service.s3.event.S3FileDeleteEvent;
import fit.iuh.se.hsshared.service.s3.event.S3FileMoveEvent;
import org.springframework.context.ApplicationEventPublisher;
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
@Slf4j
public class UserServiceImpl implements UserService {

    UserAccountRepository userAccountRepository;
    UserMapper userMapper;
    S3Service s3Service;
    ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getProfile(Long currentUserId) {
        if (currentUserId == null)
            throw new AppException(ErrorCode.INVALID_ARGUMENT, "Current user ID must not be null");
        UserAccount user = userAccountRepository.findByIdAndStatusNot(currentUserId, AccountStatus.INACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toUserResponse(user);
    }

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

    @Override
    @Transactional(readOnly = true)
    public AvatarPresignedUrlResponse generateAvatarPresignedUrl(Long currentUserId, AvatarPresignedUrlRequest request) {
        if (currentUserId == null)
            throw new AppException(ErrorCode.INVALID_ARGUMENT, "Current user ID must not be null");
        if (request == null || request.getFileName() == null || request.getFileName().trim().isEmpty())
            throw new AppException(ErrorCode.INVALID_ARGUMENT, "Yêu cầu tạo Presigned URL không hợp lệ: Thiếu tên file ảnh");

        String fileName = request.getFileName().trim();
        String lowerName = fileName.toLowerCase();
        String contentType = request.getContentType() != null ? request.getContentType().trim().toLowerCase() : "";

        if (!contentType.startsWith("image/") || contentType.equals("application/octet-stream")) {
            if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".jfif") || lowerName.endsWith(".pjpeg")) contentType = "image/jpeg";
            else if (lowerName.endsWith(".png")) contentType = "image/png";
            else if (lowerName.endsWith(".webp")) contentType = "image/webp";
            else if (lowerName.endsWith(".gif")) contentType = "image/gif";
            else if (lowerName.endsWith(".bmp")) contentType = "image/bmp";
            else if (lowerName.endsWith(".svg")) contentType = "image/svg+xml";
            else if (lowerName.endsWith(".heic") || lowerName.endsWith(".heif")) contentType = "image/heic";
            else if (lowerName.endsWith(".avif")) contentType = "image/avif";
            else if (lowerName.endsWith(".tiff") || lowerName.endsWith(".tif")) contentType = "image/tiff";
            else if (lowerName.endsWith(".ico")) contentType = "image/x-icon";
            else {
                throw new AppException(ErrorCode.INVALID_ARGUMENT, "Định dạng file ảnh không hợp lệ (hỗ trợ .jpg, .jpeg, .png, .webp, .gif, .heic, .avif, .svg...)");
            }
        }

        userAccountRepository.findByIdAndStatusNot(currentUserId, AccountStatus.INACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String s3Key = s3Service.generateObjectKey(S3Service.FOLDER_TMP_AVATARS, currentUserId, fileName);
        String uploadUrl = s3Service.generatePresignedUploadUrl(s3Key, contentType);
        String publicUrl = s3Service.getPublicUrl(s3Key);

        return AvatarPresignedUrlResponse.builder()
                .uploadUrl(uploadUrl)
                .s3Key(s3Key)
                .publicUrl(publicUrl)
                .build();
    }

    private void validateSelfUpdateRole(UserRole currentUserRole) {
        if (currentUserRole == UserRole.ADMIN
                || currentUserRole == UserRole.CARE_COORDINATOR
                || currentUserRole == UserRole.DOCTOR
                || currentUserRole == UserRole.MEMBER)
            return;
        throw new AppException(ErrorCode.ACCESS_DENIED, "Only admins, care coordinators, doctors and members can update profile here");
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
        if (request.getTimezone() != null)
            profile.setTimezone(request.getTimezone().trim());
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().trim().isEmpty()) {
            String newAvatarUrl = request.getAvatarUrl().trim();
            String newKey = s3Service.extractObjectKeyFromUrl(newAvatarUrl);
            if (newKey != null && (newKey.startsWith(S3Service.FOLDER_TMP_AVATARS) || newKey.startsWith("tmp/"))) {
                String destinationKey = newKey.replaceFirst("^tmp/", "");
                log.info("Dispatching S3FileMoveEvent for temp avatar from {} to {}", newKey, destinationKey);
                applicationEventPublisher.publishEvent(new S3FileMoveEvent(newKey, destinationKey));
                newAvatarUrl = s3Service.getPublicUrl(destinationKey);
            }

            String oldAvatarUrl = profile.getAvatarUrl();
            if (oldAvatarUrl != null && !oldAvatarUrl.equals(newAvatarUrl)) {
                String oldKey = s3Service.extractObjectKeyFromUrl(oldAvatarUrl);
                if (oldKey != null && (oldKey.startsWith(S3Service.FOLDER_AVATARS) || oldKey.startsWith(S3Service.FOLDER_TMP_AVATARS) || oldKey.startsWith("tmp/"))) {
                    log.info("Dispatching S3FileDeleteEvent for old avatar: {}", oldKey);
                    applicationEventPublisher.publishEvent(new S3FileDeleteEvent(oldKey));
                }
            }
            profile.setAvatarUrl(newAvatarUrl);
        }
    }
}
