package fit.iuh.se.hsapplication.controller.health;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hshealthrecord.dto.request.AiCallbackRequest;
import fit.iuh.se.hshealthrecord.dto.request.PresignedUrlRequest;
import fit.iuh.se.hshealthrecord.dto.response.HealthRecordResponse;
import fit.iuh.se.hshealthrecord.dto.response.HealthStatisticsResponse;
import fit.iuh.se.hshealthrecord.dto.response.PresignedUrlResponse;
import fit.iuh.se.hshealthrecord.service.user.HealthRecordService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/health-records")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HealthRecordController {

    private static final long MAX_CALLBACK_SKEW_SECONDS = 300L;

    HealthRecordService healthRecordService;

    @NonFinal
    @Value("${app.security.ai-callback.secret:${AI_CALLBACK_SECRET:}}")
    String aiCallbackSecret;

    @NonFinal
    @Value("${app.security.ai-callback.enforce:false}")
    boolean enforceAiCallbackSignature;

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

    @GetMapping("/{id}/download-url")
    public ApiResponse<PresignedUrlResponse> getRecordDownloadUrl(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long id) {
        return new ApiResponse<>(healthRecordService.getRecordDownloadUrl(currentUser.getUserId(), id));
    }

    @GetMapping("/my-records")
    public ApiResponse<PageResponse<HealthRecordResponse>> getMyRecords(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "filter", required = false) String filter) {
        int safeSize = Math.min(Math.max(1, size), 100);
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        return new ApiResponse<>(healthRecordService.getMyRecords(currentUser.getUserId(), keyword, filter, pageable));
    }

    @GetMapping("/statistics")
    public ApiResponse<HealthStatisticsResponse> getHealthStatistics(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @RequestParam(name = "period", defaultValue = "YEAR") String period,
            @RequestParam(name = "referenceDate", required = false) String referenceDate,
            @RequestParam(name = "timezone", defaultValue = "UTC") String timezone) {
        return new ApiResponse<>(healthRecordService.getHealthStatistics(currentUser.getUserId(), period, referenceDate, timezone));
    }

    @PatchMapping("/ai-callback")
    public ApiResponse<HealthRecordResponse> updateAiResult(
            @RequestHeader(value = "X-AI-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "X-AI-Signature", required = false) String signature,
            @Valid @RequestBody AiCallbackRequest request) {
        verifyAiCallbackSignature(timestamp, signature, request.getRecordId(), request.getPredictionLabel().name());
        return new ApiResponse<>(healthRecordService.updateAiResult(request));
    }

    @PatchMapping("/ai-callback/fail")
    public ApiResponse<HealthRecordResponse> markAiFailed(
            @RequestHeader(value = "X-AI-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "X-AI-Signature", required = false) String signature,
            @Valid @RequestBody fit.iuh.se.hshealthrecord.dto.request.AiCallbackFailedRequest request) {
        verifyAiCallbackSignature(timestamp, signature, request.getRecordId(), "FAILED");
        return new ApiResponse<>(healthRecordService.markAsFailed(request));
    }

    private void verifyAiCallbackSignature(String timestamp, String signature, Long recordId, String statusOrLabel) {
        String secret = aiCallbackSecret == null ? "" : aiCallbackSecret.trim();
        if (secret.isEmpty()) {
            if (enforceAiCallbackSignature) {
                log.error("AI callback rejected: enforceAiCallbackSignature=true but AI_CALLBACK_SECRET is empty");
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
            return;
        }
        boolean valid = isValidHmac(secret, timestamp, signature, recordId, statusOrLabel);
        if (!valid) {
            if (enforceAiCallbackSignature) {
                log.warn("Rejected invalid AI callback signature for recordId={}", recordId);
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
            log.warn("Invalid or missing AI callback signature for recordId={} (log-only mode)", recordId);
        }
    }

    private boolean isValidHmac(String secret, String timestamp, String signature, Long recordId, String statusOrLabel) {
        if (timestamp == null || signature == null || recordId == null || statusOrLabel == null) {
            return false;
        }
        try {
            long ts = Long.parseLong(timestamp.trim());
            long now = Instant.now().getEpochSecond();
            if (Math.abs(now - ts) > MAX_CALLBACK_SKEW_SECONDS) {
                return false;
            }
            String canonical = ts + "." + recordId + "." + statusOrLabel;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String expected = HexFormat.of().formatHex(mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
            return MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8),
                    signature.trim().toLowerCase().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }

    @GetMapping("/history/available-dates")
    public ApiResponse<java.util.List<String>> getAvailableHistoryDates(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @RequestParam(name = "timezone", defaultValue = "UTC") String timezone) {
        return new ApiResponse<>(healthRecordService.getAvailableHistoryDates(currentUser.getUserId(), timezone));
    }

    @GetMapping("/history/by-date")
    public ApiResponse<java.util.List<HealthRecordResponse>> getRecordsByDate(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @RequestParam(name = "date") String date,
            @RequestParam(name = "timezone", defaultValue = "UTC") String timezone) {
        return new ApiResponse<>(healthRecordService.getRecordsByDate(currentUser.getUserId(), date, timezone));
    }
}
