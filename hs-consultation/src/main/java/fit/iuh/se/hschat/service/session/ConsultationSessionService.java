package fit.iuh.se.hschat.service.session;

import fit.iuh.se.hschat.dto.request.AdminCreateConsultationSessionRequest;
import fit.iuh.se.hschat.dto.request.CloseConsultationRequest;
import fit.iuh.se.hschat.dto.request.RequestSessionTerminationRequest;
import fit.iuh.se.hschat.dto.request.ExtendConsultationRequest;
import fit.iuh.se.hschat.dto.response.ConsultationSessionResponse;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import org.springframework.data.domain.Pageable;

public interface ConsultationSessionService {

    ConsultationSessionResponse createSessionByAdmin(Long actorId, UserRole actorRole, AdminCreateConsultationSessionRequest request);

    ConsultationSessionResponse getSessionById(Long userId, Long sessionId);

    PageResponse<ConsultationSessionResponse> getMySessions(Long userId, Pageable pageable);

    PageResponse<ConsultationSessionResponse> getDoctorSessions(Long doctorId, Pageable pageable);

    PageResponse<ConsultationSessionResponse> getSessionsForAdmin(UserRole actorRole, Pageable pageable);

    ConsultationSessionResponse extendSession(Long actorId, UserRole actorRole, Long sessionId, ExtendConsultationRequest request);

    ConsultationSessionResponse closeSession(Long actorId, UserRole actorRole, Long sessionId, CloseConsultationRequest request);

    ConsultationSessionResponse requestTermination(Long actorId, UserRole actorRole, Long sessionId,
                                                    RequestSessionTerminationRequest request);

    void flagDisabledActiveParticipantsForReview(UserRole actorRole);

    void expireOverdueSessions(UserRole actorRole);

    void activateScheduledSessions(UserRole actorRole);
}
