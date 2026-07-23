package fit.iuh.se.hsapplication.controller.admin;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hsnotification.service.EmailService;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsuser.dto.request.AdminUserCreateRequest;
import fit.iuh.se.hsuser.dto.request.UserUpdateRequest;
import fit.iuh.se.hsuser.dto.response.AdminUserCreateResult;
import fit.iuh.se.hsuser.dto.response.UserResponse;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsuser.service.admin.AdminUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminUserController {

    AdminUserService adminUserService;
    EmailService emailService;

    @PostMapping
    public ApiResponse<UserResponse> createUser(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @Valid @RequestBody AdminUserCreateRequest request) {
        AdminUserCreateResult result = adminUserService.createUser(
                currentUser.getUserId(),
                currentUser.getRole(),
                request
        );
        UserResponse user = result.getUser();
        emailService.sendTemporaryPasswordAsync(
                user.getEmail(),
                user.getDisplayName(),
                user.getRole().name(),
                result.getTemporaryPassword()
        );
        return new ApiResponse<>(user);
    }

    @GetMapping
    public ApiResponse<PageResponse<UserResponse>> getUsers(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @RequestParam UserRole role,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return new ApiResponse<>(adminUserService.getUsers(currentUser.getUserId(), currentUser.getRole(), role, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUser(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long id) {
        return new ApiResponse<>(adminUserService.getUser(currentUser.getUserId(), currentUser.getRole(), id));
    }

    @PatchMapping("/{id}")
    public ApiResponse<UserResponse> updateUser(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        return new ApiResponse<>(adminUserService.updateUser(currentUser.getUserId(), currentUser.getRole(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long id) {
        adminUserService.deleteUser(currentUser.getUserId(), currentUser.getRole(), id);
        return new ApiResponse<>(null);
    }
}
