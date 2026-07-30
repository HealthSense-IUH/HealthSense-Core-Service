package fit.iuh.se.hschat.service.consultation.impl;

import fit.iuh.se.hschat.dto.request.ApproveConsultationRequest;
import fit.iuh.se.hschat.dto.request.CreateConsultationRequest;
import fit.iuh.se.hschat.dto.request.RejectConsultationRequest;
import fit.iuh.se.hschat.dto.response.ConsultationRequestResponse;
import fit.iuh.se.hschat.entity.ConsultationParticipant;
import fit.iuh.se.hschat.entity.ConsultationRequest;
import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.enums.ConsultationParticipantRole;
import fit.iuh.se.hschat.entity.enums.ConsultationRequestStatus;
import fit.iuh.se.hschat.entity.enums.ConsultationSourceType;
import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import fit.iuh.se.hschat.mapper.ConsultationMapper;
import fit.iuh.se.hschat.repository.ConsultationParticipantRepository;
import fit.iuh.se.hschat.repository.ConsultationRequestRepository;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hschat.service.consultation.ConsultationRequestService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConsultationRequestServiceImpl implements ConsultationRequestService {

    ConsultationRequestRepository requestRepository;
    ConsultationSessionRepository sessionRepository;
    ConsultationParticipantRepository participantRepository;
    UserAccountRepository userAccountRepository;
    ConsultationMapper mapper;

    @NonFinal
    @Value("${app.consultation.default-doctor-max-active-sessions:20}")
    int defaultDoctorMaxActiveSessions;

    @Override
    public ConsultationRequestResponse createRequest(Long memberId, CreateConsultationRequest request) {
        log.info("Creating consultation request for member {}", memberId);

        UserAccount member = userAccountRepository.findById(memberId)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));
        if (member.getRole() != UserRole.MEMBER)
            throw new AppException(ErrorCode.MEMBER_NOT_FOUND);

        if (sessionRepository.existsByMemberIdAndStatus(memberId, ConsultationStatus.ACTIVE))
            throw new AppException(ErrorCode.MEMBER_ALREADY_HAS_ACTIVE_CONSULTATION);

        if (requestRepository.existsByMemberIdAndStatus(memberId, ConsultationRequestStatus.PENDING))
            throw new AppException(ErrorCode.MEMBER_ALREADY_HAS_PENDING_CONSULTATION_REQUEST);

        Instant now = Instant.now();
        ConsultationRequest consultationRequest = ConsultationRequest.builder()
                .memberId(memberId)
                .healthRecordId(request.getHealthRecordId())
                .reason(request.getReason())
                .preferredDoctorId(request.getPreferredDoctorId())
                .status(ConsultationRequestStatus.PENDING)
                .build();

        consultationRequest = requestRepository.save(consultationRequest);
        return mapper.toRequestResponse(consultationRequest);
    }

    @Override
    public ConsultationRequestResponse approveRequest(Long adminId, Long requestId, ApproveConsultationRequest request) {
        log.info("Approving consultation request {} by admin {}", requestId, adminId);

        ConsultationRequest consultationRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_REQUEST_NOT_FOUND));

        if (consultationRequest.getStatus() != ConsultationRequestStatus.PENDING)
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS);

        Long memberId = consultationRequest.getMemberId();
        validateMember(memberId);
        validateDoctor(request.getDoctorId());
        validateConsultationPeriod(request.getEndsAt(), request.getSupportEndsAt());

        if (sessionRepository.existsByMemberIdAndStatus(memberId, ConsultationStatus.ACTIVE))
            throw new AppException(ErrorCode.MEMBER_ALREADY_HAS_ACTIVE_CONSULTATION);

        long activeConsultations = sessionRepository.countByDoctorIdAndStatus(
                request.getDoctorId(),
                ConsultationStatus.ACTIVE
        );
        if (activeConsultations >= defaultDoctorMaxActiveSessions)
            throw new AppException(ErrorCode.DOCTOR_CAPACITY_EXCEEDED);

        Instant now = Instant.now();
        Instant startedAt = request.getStartedAt() == null ? now : request.getStartedAt();
        Instant supportEndsAt = request.getSupportEndsAt() == null ? request.getEndsAt() : request.getSupportEndsAt();

        ConsultationSession session = ConsultationSession.builder()
                .memberId(memberId)
                .doctorId(request.getDoctorId())
                .createdByAdminId(adminId)
                .sourceType(ConsultationSourceType.MEMBER_REQUEST)
                .status(ConsultationStatus.ACTIVE)
                .startedAt(startedAt)
                .endsAt(request.getEndsAt())
                .supportEndsAt(supportEndsAt)
                .healthRecordId(consultationRequest.getHealthRecordId())
                .requestId(consultationRequest.getId())
                .build();

        session = sessionRepository.save(session);

        participantRepository.save(ConsultationParticipant.builder()
                .sessionId(session.getId())
                .userId(memberId)
                .role(ConsultationParticipantRole.MEMBER)
                .joinedAt(now)
                .active(true)
                .build());

        participantRepository.save(ConsultationParticipant.builder()
                .sessionId(session.getId())
                .userId(request.getDoctorId())
                .role(ConsultationParticipantRole.DOCTOR)
                .joinedAt(now)
                .active(true)
                .build());

        consultationRequest.setStatus(ConsultationRequestStatus.APPROVED);
        consultationRequest.setAssignedDoctorId(request.getDoctorId());
        consultationRequest.setConsultationSessionId(session.getId());
        consultationRequest.setReviewedByAdminId(adminId);
        consultationRequest.setReviewedAt(now);

        consultationRequest = requestRepository.save(consultationRequest);
        return mapper.toRequestResponse(consultationRequest);
    }

    @Override
    public ConsultationRequestResponse rejectRequest(Long adminId, Long requestId, RejectConsultationRequest request) {
        throw new UnsupportedOperationException("rejectRequest is not implemented yet");
    }

    @Override
    public ConsultationRequestResponse cancelMyRequest(Long memberId, Long requestId) {
        throw new UnsupportedOperationException("cancelMyRequest is not implemented yet");
    }

    @Override
    public ConsultationRequestResponse getMyRequestById(Long memberId, Long requestId) {
        throw new UnsupportedOperationException("getMyRequestById is not implemented yet");
    }

    @Override
    public PageResponse<ConsultationRequestResponse> getMyRequests(Long memberId, Pageable pageable) {
        throw new UnsupportedOperationException("getMyRequests is not implemented yet");
    }

    @Override
    public PageResponse<ConsultationRequestResponse> getRequestsForAdmin(Pageable pageable) {
        throw new UnsupportedOperationException("getRequestsForAdmin is not implemented yet");
    }

    private void validateMember(Long memberId) {
        UserAccount member = userAccountRepository.findById(memberId)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));
        if (member.getRole() != UserRole.MEMBER) {
            throw new AppException(ErrorCode.MEMBER_NOT_FOUND);
        }
    }

    private void validateDoctor(Long doctorId) {
        UserAccount doctor = userAccountRepository.findById(doctorId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));
        if (doctor.getRole() != UserRole.DOCTOR) {
            throw new AppException(ErrorCode.DOCTOR_NOT_FOUND);
        }
    }

    private void validateConsultationPeriod(Instant endsAt, Instant supportEndsAt) {
        if (supportEndsAt != null && supportEndsAt.isBefore(endsAt)) {
            throw new AppException(ErrorCode.INVALID_PARAMETER, "Support end time must not be before consultation end time");
        }
    }
}
