package fit.iuh.se.hschat.service.dispatch.offer;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DoctorOfferStore {
    enum CreateResult { CREATED, DOCTOR_ALREADY_HELD, QUEUE_ALREADY_HELD }
    enum AcceptResult { ACCEPTED, ALREADY_ACCEPTED, EXPIRED, STALE }
    enum ConfirmResult { CONFIRMED, ALREADY_CONFIRMED, EXPIRED, STALE }

    CreateResult create(DoctorOffer offer);
    Optional<DoctorOffer> findById(String offerId);
    Optional<DoctorOffer> findByDoctorId(Long doctorId);
    Optional<DoctorOffer> findByQueueEntryId(Long queueEntryId);
    AcceptResult accept(String offerId, Long doctorId, Instant now, Instant memberConfirmExpiresAt);
    ConfirmResult confirm(String offerId, Long queueEntryId, Long memberId, Long doctorId, Instant now);
    boolean restoreMemberConfirmation(DoctorOffer offer);
    boolean release(DoctorOffer offer);
    List<String> dueDoctorOfferIds(Instant now, int limit);
    List<String> dueMemberConfirmationOfferIds(Instant now, int limit);
    Set<Long> heldDoctorIds(Collection<Long> doctorIds);
}
