package fit.iuh.se.hsapplication.controller.doctor;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hschat.dto.response.statistics.DoctorConsultationStatisticsResponse;
import fit.iuh.se.hschat.dto.response.statistics.DoctorPatientConsultationResponse;
import fit.iuh.se.hschat.dto.response.statistics.DoctorPatientDetailResponse;
import fit.iuh.se.hschat.dto.response.statistics.DoctorPatientHealthRecordResponse;
import fit.iuh.se.hschat.dto.response.statistics.DoctorPatientListItemResponse;
import fit.iuh.se.hschat.dto.response.statistics.DoctorPatientMedicalHistoryResponse;
import fit.iuh.se.hschat.dto.response.statistics.DoctorPatientStatisticsResponse;
import fit.iuh.se.hschat.service.statistics.DoctorStatisticsService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@Validated
@RestController
@RequestMapping("/api/doctor/statistics")
@RequiredArgsConstructor
public class DoctorStatisticsController {

    private final DoctorStatisticsService service;

    @GetMapping("/consultations")
    public ApiResponse<DoctorConsultationStatisticsResponse> getConsultationStatistics(
            @AuthenticationPrincipal UserAuthentication actor,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return new ApiResponse<>(service.getConsultationStatistics(doctorId(actor), from, to));
    }

    @GetMapping("/patients/overview")
    public ApiResponse<DoctorPatientStatisticsResponse> getPatientStatistics(
            @AuthenticationPrincipal UserAuthentication actor,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return new ApiResponse<>(service.getPatientStatistics(doctorId(actor), from, to));
    }

    @GetMapping("/patients")
    public ApiResponse<PageResponse<DoctorPatientListItemResponse>> getPatients(
            @AuthenticationPrincipal UserAuthentication actor,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "") @Size(max = 120) String keyword,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return new ApiResponse<>(service.getPatients(
                doctorId(actor), from, to, keyword, PageRequest.of(page - 1, size)));
    }

    @GetMapping("/patients/{memberId}")
    public ApiResponse<DoctorPatientDetailResponse> getPatient(
            @AuthenticationPrincipal UserAuthentication actor,
            @PathVariable @Min(1) Long memberId) {
        return new ApiResponse<>(service.getPatient(doctorId(actor), memberId));
    }

    @GetMapping("/patients/{memberId}/consultations")
    public ApiResponse<PageResponse<DoctorPatientConsultationResponse>> getPatientConsultations(
            @AuthenticationPrincipal UserAuthentication actor,
            @PathVariable @Min(1) Long memberId,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return new ApiResponse<>(service.getPatientConsultations(
                doctorId(actor), memberId, PageRequest.of(page - 1, size)));
    }

    @GetMapping("/patients/{memberId}/medical-history")
    public ApiResponse<PageResponse<DoctorPatientMedicalHistoryResponse>> getPatientMedicalHistory(
            @AuthenticationPrincipal UserAuthentication actor,
            @PathVariable @Min(1) Long memberId,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return new ApiResponse<>(service.getPatientMedicalHistory(
                doctorId(actor), memberId, PageRequest.of(page - 1, size)));
    }

    @GetMapping("/patients/{memberId}/health-records")
    public ApiResponse<PageResponse<DoctorPatientHealthRecordResponse>> getPatientHealthRecords(
            @AuthenticationPrincipal UserAuthentication actor,
            @PathVariable @Min(1) Long memberId,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return new ApiResponse<>(service.getPatientHealthRecords(
                doctorId(actor), memberId, PageRequest.of(page - 1, size)));
    }

    private Long doctorId(UserAuthentication actor) {
        if (actor == null) throw new AppException(ErrorCode.UNAUTHORIZED);
        if (actor.getRole() != UserRole.DOCTOR) throw new AppException(ErrorCode.ACCESS_DENIED);
        return actor.getUserId();
    }
}
