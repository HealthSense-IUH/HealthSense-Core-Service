package fit.iuh.se.hschat.service.consultation;

import fit.iuh.se.hschat.dto.request.ApproveConsultationRequest;
import fit.iuh.se.hschat.dto.request.CreateConsultationRequest;
import fit.iuh.se.hschat.dto.request.RejectConsultationRequest;
import fit.iuh.se.hschat.dto.response.ConsultationRequestResponse;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface ConsultationRequestService {

    ConsultationRequestResponse createRequest(Long memberId, CreateConsultationRequest request);

    ConsultationRequestResponse approveRequest(Long adminId, String requestId, ApproveConsultationRequest request);

    ConsultationRequestResponse rejectRequest(Long adminId, String requestId, RejectConsultationRequest request);

    ConsultationRequestResponse cancelMyRequest(Long memberId, String requestId);

    ConsultationRequestResponse getMyRequestById(Long memberId, String requestId);

    PageResponse<ConsultationRequestResponse> getMyRequests(Long memberId, Pageable pageable);

    PageResponse<ConsultationRequestResponse> getRequestsForAdmin(Pageable pageable);
}
