package fit.iuh.se.hschat.service.message;

import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.enums.ConsultationParticipantRole;

public interface SupportHoursPolicy {

    boolean canSendNow(ConsultationSession session, ConsultationParticipantRole role);
}
