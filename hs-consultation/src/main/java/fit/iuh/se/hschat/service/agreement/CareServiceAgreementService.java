package fit.iuh.se.hschat.service.agreement;

import fit.iuh.se.hschat.dto.response.CareServiceAgreementResponse;
import fit.iuh.se.hschat.entity.*;

public interface CareServiceAgreementService {

    CareServiceAgreement createForReservation(ConsultationRequest request);

    CareServiceAgreementResponse getCurrentForMember(Long memberId, Long requestId);

    CareServiceAgreementResponse accept(Long memberId, Long requestId, Long agreementId);

    CareServiceAgreement requireAcceptedForUpdate(ConsultationRequest request);

    void consume(CareServiceAgreement agreement);

    void invalidateCurrent(Long requestId, String reason);

    CareServiceAgreement createForRenewal(
            ConsultationRenewal renewal, CareServicePackage carePackage, DoctorCareProfile profile);

    CareServiceAgreementResponse getRenewalAgreement(Long memberId, Long renewalId);

    CareServiceAgreementResponse acceptRenewal(Long memberId, Long renewalId, Long agreementId);

    CareServiceAgreement requireAcceptedForRenewal(ConsultationRenewal renewal);

    void invalidateRenewal(Long renewalId, String reason);
}
