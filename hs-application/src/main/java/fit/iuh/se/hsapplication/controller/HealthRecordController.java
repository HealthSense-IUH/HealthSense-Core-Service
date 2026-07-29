package fit.iuh.se.hsapplication.controller;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hshealthrecord.dto.request.AiCallbackRequest;
import fit.iuh.se.hshealthrecord.dto.request.PresignedUrlRequest;
import fit.iuh.se.hshealthrecord.dto.response.HealthRecordResponse;
import fit.iuh.se.hshealthrecord.dto.response.PresignedUrlResponse;
import fit.iuh.se.hshealthrecord.service.HealthRecordService;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

@Validated
@RestController
@RequestMapping("/api/health-records")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HealthRecordController {

    HealthRecordService healthRecordService;

    @PostMapping("/presigned-url")
    public ApiResponse<PresignedUrlResponse> createPresignedUrl(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @Valid @RequestBody PresignedUrlRequest request) {
        return new ApiResponse<>(healthRecordService.createPresignedUploadUrl(currentUser.getUserId(), request));
    }

    @PostMapping(value = "/upload-direct", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<HealthRecordResponse> uploadDirect(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @RequestParam("file") MultipartFile file) {
        return new ApiResponse<>(healthRecordService.uploadDirectAndProcess(currentUser.getUserId(), file));
    }

    @PostMapping("/{id}/confirm")
    public ApiResponse<HealthRecordResponse> confirmUpload(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long id) {
        return new ApiResponse<>(healthRecordService.confirmUpload(currentUser.getUserId(), id));
    }

    @GetMapping("/{id}")
    public ApiResponse<HealthRecordResponse> getRecordById(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long id) {
        return new ApiResponse<>(healthRecordService.getRecordById(currentUser.getUserId(), id));
    }

    @GetMapping("/my-records")
    public ApiResponse<PageResponse<HealthRecordResponse>> getMyRecords(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return new ApiResponse<>(healthRecordService.getMyRecords(currentUser.getUserId(), pageable));
    }

    @PatchMapping("/ai-callback")
    public ApiResponse<HealthRecordResponse> updateAiResult(
            @Valid @RequestBody AiCallbackRequest request) {
        return new ApiResponse<>(healthRecordService.updateAiResult(request));
    }
}
