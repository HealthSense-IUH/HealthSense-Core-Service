package fit.iuh.se.hschat.service.agreement;

import fit.iuh.se.hschat.dto.response.CareServiceAgreementResponse;
import fit.iuh.se.hschat.entity.CareServiceAgreement;
import fit.iuh.se.hschat.entity.ConsultationRequest;
import fit.iuh.se.hschat.entity.ConsultationRenewal;
import fit.iuh.se.hschat.entity.CareServicePackage;
import fit.iuh.se.hschat.entity.DoctorCareProfile;

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
