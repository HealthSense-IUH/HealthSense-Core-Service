package fit.iuh.se.hschat.service.dispatch;

import fit.iuh.se.hschat.dto.response.DoctorConsultationOfferResponse;

public interface DoctorOfferService {
    boolean dispatchOne();
    DoctorConsultationOfferResponse getCurrentOffer(Long doctorId);
    DoctorConsultationOfferResponse accept(Long doctorId, String offerId);
    DoctorConsultationOfferResponse reject(Long doctorId, String offerId);
    void processDoctorTimeout(String offerId);
    void processMemberConfirmationTimeout(String offerId);
    void reconcileMissingOffers();
}
