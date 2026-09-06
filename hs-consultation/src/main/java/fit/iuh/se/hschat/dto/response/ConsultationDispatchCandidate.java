package fit.iuh.se.hschat.dto.response;

import java.time.LocalDate;

public record ConsultationDispatchCandidate(
        Long queueEntryId,
        Long requestId,
        Long memberId,
        LocalDate queueDate,
        Long queueNumber,
        Long doctorId) {
}
