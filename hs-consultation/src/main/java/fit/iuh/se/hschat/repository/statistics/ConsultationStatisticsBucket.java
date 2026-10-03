package fit.iuh.se.hschat.repository.statistics;

import java.time.LocalDate;

public record ConsultationStatisticsBucket(LocalDate bucketStart, long count) {
}
