package fit.iuh.se.hschat.service.authorization;

import fit.iuh.se.hschat.dto.response.EpisodeHealthRecordAuthorizationResponse;
import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.EpisodeHealthRecordAuthorization;

import java.util.Collection;
import java.util.List;

public interface EpisodeHealthRecordAuthorizationService {

    List<EpisodeHealthRecordAuthorization> authorizeInitialRecords(
            ConsultationSession session, Collection<Long> healthRecordIds);

    EpisodeHealthRecordAuthorization authorizeAdminInitialRecord(
            ConsultationSession session, Long healthRecordId, Long adminId);

    EpisodeHealthRecordAuthorizationResponse shareDuringActiveCare(
            Long memberId, Long sessionId, Long healthRecordId);

    void authorizeCreatedRecord(Long memberId, Long healthRecordId);

    EpisodeHealthRecordAuthorization requireDoctorReadAccess(
            Long doctorId, ConsultationSession session, Long healthRecordId);

    EpisodeHealthRecordAuthorization requireDoctorCurrentWriteAccess(
            Long doctorId, ConsultationSession session, Long healthRecordId);

    List<EpisodeHealthRecordAuthorization> getSessionAuthorizations(Long sessionId);
}
