package fit.iuh.se.hsapplication.controller.user;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import fit.iuh.se.hsuser.dto.request.AvatarPresignedUrlRequest;
import fit.iuh.se.hsuser.dto.request.UserProfileUpdateRequest;
import fit.iuh.se.hsuser.dto.response.AvatarPresignedUrlResponse;
import fit.iuh.se.hsuser.dto.response.UserResponse;
import fit.iuh.se.hsuser.service.user.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserResponse> getProfile(@AuthenticationPrincipal UserAuthentication currentUser) {
        return new ApiResponse<>(userService.getProfile(currentUser.getUserId()));
    }

    @PatchMapping("/me")
    public ApiResponse<UserResponse> updateProfile(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        return new ApiResponse<>(userService.updateProfile(currentUser.getUserId(), currentUser.getRole(), request));
    }

    @PostMapping("/me/avatar/presigned-url")
    public ApiResponse<AvatarPresignedUrlResponse> generateAvatarPresignedUrl(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @Valid @RequestBody AvatarPresignedUrlRequest request) {
        return new ApiResponse<>(userService.generateAvatarPresignedUrl(currentUser.getUserId(), request));
    }
}
