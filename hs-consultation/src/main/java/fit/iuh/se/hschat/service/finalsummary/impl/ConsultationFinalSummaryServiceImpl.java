package fit.iuh.se.hschat.service.finalsummary.impl;

import fit.iuh.se.hschat.dto.request.UpsertConsultationFinalSummaryRequest;
import fit.iuh.se.hschat.dto.response.ConsultationFinalSummaryResponse;
import fit.iuh.se.hschat.entity.ConsultationFinalSummary;
import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.enums.ConsultationFinalSummaryStatus;
import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import fit.iuh.se.hschat.repository.ConsultationFinalSummaryRepository;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hschat.service.finalsummary.ConsultationFinalSummaryService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConsultationFinalSummaryServiceImpl implements ConsultationFinalSummaryService {

    ConsultationSessionRepository sessionRepository;
    ConsultationFinalSummaryRepository summaryRepository;

    @Override
    @Transactional(readOnly = true)
    public ConsultationFinalSummaryResponse getForDoctor(Long doctorId, Long sessionId) {
        getAssignedDoctorSession(doctorId, sessionId);
        return summaryRepository.findBySessionId(sessionId)
                .map(this::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.ENTITY_NOT_FOUND, "Final care summary not found"));
    }

    @Override
    @Transactional
    public ConsultationFinalSummaryResponse upsertDraft(Long doctorId, Long sessionId, UpsertConsultationFinalSummaryRequest request) {
        ConsultationSession session = getAssignedDoctorSession(doctorId, sessionId);
        if (session.getStatus() != ConsultationStatus.ACTIVE && session.getStatus() != ConsultationStatus.COMPLETED)
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS, "Final summary draft can be edited only for active or completed sessions");

        ConsultationFinalSummary summary = summaryRepository.findBySessionId(sessionId)
                .orElseGet(() -> ConsultationFinalSummary.builder()
                        .sessionId(sessionId)
                        .createdByDoctorId(doctorId)
                        .status(ConsultationFinalSummaryStatus.DRAFT)
                        .build());

        if (summary.getStatus() == ConsultationFinalSummaryStatus.FINALIZED)
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS, "Finalized care summary cannot be edited");
        if (!summary.getCreatedByDoctorId().equals(doctorId))
            throw new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED);

        apply(summary, request);
        summary.setStatus(ConsultationFinalSummaryStatus.DRAFT);
        summary = summaryRepository.save(summary);
        return toResponse(summary);
    }

    @Override
    @Transactional
    public ConsultationFinalSummaryResponse finalizeSummary(Long doctorId, Long sessionId) {
        ConsultationSession session = getAssignedDoctorSession(doctorId, sessionId);
        if (session.getStatus() != ConsultationStatus.COMPLETED)
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS, "Final summary can be finalized only after session completion");

        ConsultationFinalSummary summary = summaryRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.ENTITY_NOT_FOUND, "Final care summary draft not found"));
        if (!summary.getCreatedByDoctorId().equals(doctorId))
            throw new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED);
        if (summary.getStatus() == ConsultationFinalSummaryStatus.FINALIZED)
            return toResponse(summary);

        summary.setStatus(ConsultationFinalSummaryStatus.FINALIZED);
        summary.setFinalizedAt(Instant.now());
        summary = summaryRepository.save(summary);
        return toResponse(summary);
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultationFinalSummaryResponse getForMember(Long memberId, Long sessionId) {
        ConsultationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_NOT_FOUND));
        if (!session.getMemberId().equals(memberId))
            throw new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED);
        return getFinalizedSummary(sessionId);
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultationFinalSummaryResponse getForAdmin(UserRole actorRole, Long sessionId) {
        validateConsultationManager(actorRole);
        if (!sessionRepository.existsById(sessionId))
            throw new AppException(ErrorCode.CONSULTATION_NOT_FOUND);
        return getFinalizedSummary(sessionId);
    }

    private ConsultationSession getAssignedDoctorSession(Long doctorId, Long sessionId) {
        return sessionRepository.findByIdAndDoctorId(sessionId, doctorId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED));
    }

    private ConsultationFinalSummaryResponse getFinalizedSummary(Long sessionId) {
        ConsultationFinalSummary summary = summaryRepository.findBySessionId(sessionId)
                .filter(item -> item.getStatus() == ConsultationFinalSummaryStatus.FINALIZED)
                .orElseThrow(() -> new AppException(ErrorCode.ENTITY_NOT_FOUND, "Final care summary has not been finalized"));
        return toResponse(summary);
    }

    private void validateConsultationManager(UserRole actorRole) {
        if (actorRole == UserRole.SUPER_ADMIN
                || actorRole == UserRole.ADMIN
                || actorRole == UserRole.CARE_COORDINATOR)
            return;
        throw new AppException(ErrorCode.ACCESS_DENIED, "You are not allowed to view final care summaries");
    }

    private void apply(ConsultationFinalSummary summary, UpsertConsultationFinalSummaryRequest request) {
        summary.setSummary(request.getSummary());
        summary.setObservations(request.getObservations());
        summary.setRecommendations(request.getRecommendations());
        summary.setFollowUpRecommendation(request.getFollowUpRecommendation());
    }

    private ConsultationFinalSummaryResponse toResponse(ConsultationFinalSummary summary) {
        return ConsultationFinalSummaryResponse.builder()
                .id(summary.getId())
                .sessionId(summary.getSessionId())
                .status(summary.getStatus())
                .summary(summary.getSummary())
                .observations(summary.getObservations())
                .recommendations(summary.getRecommendations())
                .followUpRecommendation(summary.getFollowUpRecommendation())
                .createdByDoctorId(summary.getCreatedByDoctorId())
                .finalizedAt(summary.getFinalizedAt())
                .createdAt(summary.getCreatedAt())
                .updatedAt(summary.getUpdatedAt())
                .build();
    }
}
