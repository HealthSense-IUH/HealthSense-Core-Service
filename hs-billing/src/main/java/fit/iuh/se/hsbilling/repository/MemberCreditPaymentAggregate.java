package fit.iuh.se.hsbilling.repository;

public interface MemberCreditPaymentAggregate {
    Long getMemberId();

    Long getTotalPaidVnd();

    Long getTotalPurchasedCredits();

    Long getSuccessfulOrderCount();
}
