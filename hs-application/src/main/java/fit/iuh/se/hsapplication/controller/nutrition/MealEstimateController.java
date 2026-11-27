package fit.iuh.se.hsapplication.controller.nutrition;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hsassistant.dto.MealEstimateResponse;
import fit.iuh.se.hsassistant.service.MealEstimateService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Ước tính calo & dinh dưỡng tim mạch từ ảnh món ăn (multipart/form-data) hoặc mô tả văn bản (JSON / multipart).
 * Mọi người dùng đã đăng nhập đều sử dụng được; hội viên sẽ được chấm theo đơn ăn uống cá nhân.
 */
@RestController
@RequestMapping("/api/nutrition")
@RequiredArgsConstructor
public class MealEstimateController {

    private final MealEstimateService mealEstimateService;

    public record TextMealEstimateRequest(String description) {}

    @PostMapping(value = "/estimate-meal", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MealEstimateResponse> estimateMealMultipart(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "description", required = false) String description,
            @RequestHeader(value = "lang", required = false) String lang) {
        byte[] imageBytes = null;
        String contentType = null;
        if (image != null && !image.isEmpty()) {
            try {
                imageBytes = image.getBytes();
                contentType = image.getContentType();
            } catch (IOException e) {
                throw new AppException(ErrorCode.INVALID_ARGUMENT, "Could not read uploaded meal image.");
            }
        }
        return new ApiResponse<>(mealEstimateService.estimate(
                currentUser.getUserId(),
                currentUser.getRole(),
                imageBytes,
                contentType,
                description,
                lang
        ));
    }

    @PostMapping(value = "/estimate-meal", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<MealEstimateResponse> estimateMealText(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @RequestBody(required = false) TextMealEstimateRequest request,
            @RequestHeader(value = "lang", required = false) String lang) {
        String description = request == null ? null : request.description();
        return new ApiResponse<>(mealEstimateService.estimate(
                currentUser.getUserId(),
                currentUser.getRole(),
                null,
                null,
                description,
                lang
        ));
    }
}
