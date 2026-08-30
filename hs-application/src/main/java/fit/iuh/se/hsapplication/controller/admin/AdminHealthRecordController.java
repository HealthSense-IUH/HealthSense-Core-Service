package fit.iuh.se.hsapplication.controller.admin;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hshealthrecord.dto.request.AdminCreateHealthRecordRequest;
import fit.iuh.se.hshealthrecord.dto.response.HealthRecordResponse;
import fit.iuh.se.hshealthrecord.entity.enums.PredictionLabel;
import fit.iuh.se.hshealthrecord.entity.enums.RecordStatus;
import fit.iuh.se.hshealthrecord.service.admin.AdminHealthRecordService;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@Validated
@RestController
@RequestMapping("/api/admin/health-records")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminHealthRecordController {

    AdminHealthRecordService adminHealthRecordService;

    @PostMapping
    public ApiResponse<HealthRecordResponse> createRecordForMember(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @RequestBody @Valid AdminCreateHealthRecordRequest request) {
        return new ApiResponse<>(adminHealthRecordService.createRecordForMember(currentUser.getUserId(), request));
    }

    @GetMapping
    public ApiResponse<PageResponse<HealthRecordResponse>> getRecords(
            @RequestParam(name = "memberId", required = false) Long memberId,
            @RequestParam(name = "status", required = false) RecordStatus status,
            @RequestParam(name = "predictionLabel", required = false) PredictionLabel predictionLabel,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @RequestParam(name = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate,
            @RequestParam(name = "page", defaultValue = "1") @Min(1) int page,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return new ApiResponse<>(adminHealthRecordService.getRecords(
                memberId,
                status,
                predictionLabel,
                keyword,
                fromDate,
                toDate,
                pageable
        ));
    }

    @GetMapping("/{id}")
    public ApiResponse<HealthRecordResponse> getRecord(
            @AuthenticationPrincipal UserAuthentication currentUser, @PathVariable Long id) {
        return new ApiResponse<>(adminHealthRecordService.getRecord(currentUser.getUserId(), currentUser.getRole(), id));
    }

    @GetMapping("/statistics")
    public ApiResponse<Object> getSystemStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate
    ) {
        return new ApiResponse<>(adminHealthRecordService.getSystemStatistics(fromDate, toDate));

    }
}
