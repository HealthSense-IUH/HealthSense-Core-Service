package fit.iuh.se.hschat.service.request;

import fit.iuh.se.hschat.dto.request.ApproveConsultationRequest;
import fit.iuh.se.hschat.dto.request.CreateConsultationRequest;
import fit.iuh.se.hschat.dto.request.RejectConsultationRequest;
import fit.iuh.se.hschat.dto.request.RequestMoreConsultationInfoRequest;
import fit.iuh.se.hschat.dto.request.SubmitConsultationMoreInfoRequest;
import fit.iuh.se.hschat.dto.response.ConsultationRequestReviewResponse;
import fit.iuh.se.hschat.dto.response.DoctorCandidateResponse;
import fit.iuh.se.hschat.dto.response.ConsultationRequestResponse;
import fit.iuh.se.hschat.entity.enums.ConsultationRequestStatus;
import fit.iuh.se.hschat.entity.enums.DoctorSpecialty;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface ConsultationRequestService {

    ConsultationRequestResponse createRequest(Long memberId, CreateConsultationRequest request);

    ConsultationRequestResponse approveRequest(Long actorId, UserRole actorRole, Long requestId, ApproveConsultationRequest request);

    ConsultationRequestResponse rejectRequest(Long actorId, UserRole actorRole, Long requestId, RejectConsultationRequest request);

    ConsultationRequestResponse requestMoreInfo(Long actorId, UserRole actorRole, Long requestId, RequestMoreConsultationInfoRequest request);

    ConsultationRequestResponse submitMoreInfo(Long memberId, Long requestId, SubmitConsultationMoreInfoRequest request);

    ConsultationRequestResponse cancelMyRequest(Long memberId, Long requestId);

    ConsultationRequestResponse getMyRequestById(Long memberId, Long requestId);

    PageResponse<ConsultationRequestResponse> getMyRequests(Long memberId, Pageable pageable);

    PageResponse<ConsultationRequestResponse> getRequestsForAdmin(
            UserRole actorRole,
            ConsultationRequestStatus status,
            Long memberId,
            Long preferredDoctorId,
            Long assignedDoctorId,
            Instant fromDate,
            Instant toDate,
            Pageable pageable
    );

    ConsultationRequestReviewResponse getRequestReviewById(UserRole actorRole, Long requestId);

    PageResponse<DoctorCandidateResponse> getDoctorCandidates(
            UserRole actorRole,
            Long requestId,
            DoctorSpecialty specialty,
            String keyword,
            Boolean eligibleOnly,
            Pageable pageable
    );

    void expireWaitingPaymentRequests(UserRole actorRole);
}
