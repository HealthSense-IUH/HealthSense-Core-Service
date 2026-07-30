package fit.iuh.se.hschat.service.session;

import fit.iuh.se.hschat.dto.request.AdminCreateConsultationSessionRequest;
import fit.iuh.se.hschat.dto.request.CloseConsultationRequest;
import fit.iuh.se.hschat.dto.request.ExtendConsultationRequest;
import fit.iuh.se.hschat.dto.response.ConsultationSessionResponse;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface ConsultationSessionService {

    ConsultationSessionResponse createSessionByAdmin(Long adminId, AdminCreateConsultationSessionRequest request);

    ConsultationSessionResponse getSessionById(Long userId, Long sessionId);

    PageResponse<ConsultationSessionResponse> getMySessions(Long userId, Pageable pageable);

    PageResponse<ConsultationSessionResponse> getDoctorSessions(Long doctorId, Pageable pageable);

    PageResponse<ConsultationSessionResponse> getSessionsForAdmin(Pageable pageable);

    ConsultationSessionResponse extendSession(Long adminId, Long sessionId, ExtendConsultationRequest request);

    ConsultationSessionResponse closeSession(Long adminId, Long sessionId, CloseConsultationRequest request);

    void expireOverdueSessions();
}
