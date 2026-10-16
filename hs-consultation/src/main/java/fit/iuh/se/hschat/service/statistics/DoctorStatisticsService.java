package fit.iuh.se.hschat.service.statistics;

import fit.iuh.se.hschat.dto.response.statistics.DoctorConsultationStatisticsResponse;
import fit.iuh.se.hschat.dto.response.statistics.DoctorPatientConsultationResponse;
import fit.iuh.se.hschat.dto.response.statistics.DoctorPatientDetailResponse;
import fit.iuh.se.hschat.dto.response.statistics.DoctorPatientHealthRecordResponse;
import fit.iuh.se.hschat.dto.response.statistics.DoctorPatientListItemResponse;
import fit.iuh.se.hschat.dto.response.statistics.DoctorPatientMedicalHistoryResponse;
import fit.iuh.se.hschat.dto.response.statistics.DoctorPatientStatisticsResponse;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface DoctorStatisticsService {

    DoctorConsultationStatisticsResponse getConsultationStatistics(Long doctorId, Instant from, Instant to);

    DoctorPatientStatisticsResponse getPatientStatistics(Long doctorId, Instant from, Instant to);

    PageResponse<DoctorPatientListItemResponse> getPatients(
            Long doctorId, Instant from, Instant to, String keyword, Pageable pageable);

    DoctorPatientDetailResponse getPatient(Long doctorId, Long memberId);

    PageResponse<DoctorPatientConsultationResponse> getPatientConsultations(
            Long doctorId, Long memberId, Pageable pageable);

    PageResponse<DoctorPatientMedicalHistoryResponse> getPatientMedicalHistory(
            Long doctorId, Long memberId, Pageable pageable);

    PageResponse<DoctorPatientHealthRecordResponse> getPatientHealthRecords(
            Long doctorId, Long memberId, Pageable pageable);
}
