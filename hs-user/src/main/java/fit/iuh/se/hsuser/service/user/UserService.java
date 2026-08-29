package fit.iuh.se.hsuser.service.user;

import fit.iuh.se.hsuser.dto.request.AvatarPresignedUrlRequest;
import fit.iuh.se.hsuser.dto.request.IdentityCardPresignedUrlRequest;
import fit.iuh.se.hsuser.dto.request.UserProfileUpdateRequest;
import fit.iuh.se.hsuser.dto.response.AvatarPresignedUrlResponse;
import fit.iuh.se.hsuser.dto.response.IdentityCardPresignedUrlResponse;
import fit.iuh.se.hsuser.dto.response.UserResponse;
import fit.iuh.se.hsuser.entity.enums.UserRole;

/**
 * @author : user664dntp
 * @mailto : phatdang19052004@gmail.com
 * @created : 23/07/2026, Thursday
 **/
public interface UserService {

    UserResponse getProfile(Long currentUserId);

    UserResponse updateProfile(Long currentUserId, UserRole currentUserRole, UserProfileUpdateRequest request);

    AvatarPresignedUrlResponse generateAvatarPresignedUrl(Long currentUserId, AvatarPresignedUrlRequest request);

    IdentityCardPresignedUrlResponse generateIdentityCardPresignedUrl(Long currentUserId, IdentityCardPresignedUrlRequest request);
}
