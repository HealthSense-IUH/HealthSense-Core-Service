package fit.iuh.se.hsbilling.repository;

import java.time.Instant;

public interface CreditOrderAggregateRepository {
    CreditPaymentAggregate summarizeSuccessfulPayments(Long memberId, Instant from, Instant to);
}
