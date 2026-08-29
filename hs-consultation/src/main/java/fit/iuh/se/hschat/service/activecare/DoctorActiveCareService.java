package fit.iuh.se.hschat.service.activecare;

import fit.iuh.se.hschat.dto.response.DoctorConsultationDetailResponse;
import fit.iuh.se.hschat.dto.response.DoctorConsultationSessionResponse;
import fit.iuh.se.hschat.dto.response.DoctorScopedHealthRecordResponse;
import fit.iuh.se.hschat.dto.response.RawHealthRecordArtifactResponse;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface DoctorActiveCareService {

    PageResponse<DoctorConsultationSessionResponse> getAssignedSessions(Long doctorId, Pageable pageable);

    DoctorConsultationDetailResponse getSessionDetail(Long doctorId, Long sessionId);

    PageResponse<DoctorScopedHealthRecordResponse> getScopedHealthRecords(Long doctorId, Long sessionId, Pageable pageable);

    DoctorScopedHealthRecordResponse getScopedHealthRecord(Long doctorId, Long sessionId, Long recordId);

    RawHealthRecordArtifactResponse getRawArtifact(Long doctorId, Long sessionId, Long recordId);

    DoctorScopedHealthRecordResponse markAttentionReviewed(Long doctorId, Long sessionId, Long recordId);
}
