package fit.iuh.se.hschat.service.session.impl;

import fit.iuh.se.hschat.dto.request.AdminCreateConsultationSessionRequest;
import fit.iuh.se.hschat.dto.request.CloseConsultationRequest;
import fit.iuh.se.hschat.dto.request.ExtendConsultationRequest;
import fit.iuh.se.hschat.dto.response.ConsultationSessionResponse;
import fit.iuh.se.hschat.mapper.ConsultationMapper;
import fit.iuh.se.hschat.repository.ConsultationParticipantRepository;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hschat.service.session.ConsultationSessionService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConsultationSessionServiceImpl implements ConsultationSessionService {

    ConsultationSessionRepository sessionRepository;
    ConsultationParticipantRepository participantRepository;
    ConsultationMapper mapper;

    @Override
    public ConsultationSessionResponse createSessionByAdmin(Long adminId, AdminCreateConsultationSessionRequest request) {
        throw new UnsupportedOperationException("createSessionByAdmin is not implemented yet");
    }

    @Override
    public ConsultationSessionResponse getSessionById(Long userId, Long sessionId) {
        if (!participantRepository.existsBySessionIdAndUserIdAndActiveTrue(sessionId, userId))
            throw new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED);

        return sessionRepository.findById(sessionId)
                .map(mapper::toSessionResponse)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_NOT_FOUND));
    }

    @Override
    public PageResponse<ConsultationSessionResponse> getMySessions(Long userId, Pageable pageable) {
        Page<ConsultationSessionResponse> page = sessionRepository
                .findByMemberIdOrderByLastMessageAtDesc(userId, pageable)
                .map(mapper::toSessionResponse);
        return new PageResponse<>(page);
    }

    @Override
    public PageResponse<ConsultationSessionResponse> getDoctorSessions(Long doctorId, Pageable pageable) {
        Page<ConsultationSessionResponse> page = sessionRepository
                .findByDoctorIdOrderByLastMessageAtDesc(doctorId, pageable)
                .map(mapper::toSessionResponse);
        return new PageResponse<>(page);
    }

    @Override
    public PageResponse<ConsultationSessionResponse> getSessionsForAdmin(Pageable pageable) {
        Page<ConsultationSessionResponse> page = sessionRepository
                .findAll(pageable)
                .map(mapper::toSessionResponse);
        return new PageResponse<>(page);
    }

    @Override
    public ConsultationSessionResponse extendSession(Long adminId, Long sessionId, ExtendConsultationRequest request) {
        throw new UnsupportedOperationException("extendSession is not implemented yet");
    }

    @Override
    public ConsultationSessionResponse closeSession(Long adminId, Long sessionId, CloseConsultationRequest request) {
        throw new UnsupportedOperationException("closeSession is not implemented yet");
    }

    @Override
    public void expireOverdueSessions() {
        throw new UnsupportedOperationException("expireOverdueSessions is not implemented yet");
    }
}
