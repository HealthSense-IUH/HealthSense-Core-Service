package fit.iuh.se.hsbilling.repository;

import fit.iuh.se.hsbilling.entity.CreditPurchaseOrder;
import fit.iuh.se.hsbilling.entity.enums.CreditOrderStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.Predicate;

import java.time.Instant;
import java.util.ArrayList;

public class CreditOrderAggregateRepositoryImpl implements CreditOrderAggregateRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public CreditPaymentAggregate summarizeSuccessfulPayments(Long memberId, Instant from, Instant to) {
        var builder = entityManager.getCriteriaBuilder();
        var query = builder.createQuery(Object[].class);
        var order = query.from(CreditPurchaseOrder.class);
        var predicates = new ArrayList<Predicate>();
        predicates.add(builder.equal(order.get("status"), CreditOrderStatus.PAID));
        if (memberId != null) predicates.add(builder.equal(order.get("memberId"), memberId));
        if (from != null) predicates.add(builder.greaterThanOrEqualTo(order.get("paidAt"), from));
        if (to != null) predicates.add(builder.lessThan(order.get("paidAt"), to));
        query.multiselect(
                builder.sum(order.<Long>get("amountVnd")),
                builder.sum(order.<Long>get("creditQuantity")),
                builder.count(order.get("id")),
                builder.countDistinct(order.get("memberId")),
                builder.least(order.<Instant>get("paidAt")),
                builder.greatest(order.<Instant>get("paidAt")))
                .where(predicates.toArray(Predicate[]::new));

        Object[] result = entityManager.createQuery(query).getSingleResult();
        return new CreditPaymentAggregate(number(result[0]), number(result[1]), number(result[2]), number(result[3]),
                (Instant) result[4], (Instant) result[5]);
    }

    private long number(Object value) {
        return value == null ? 0 : ((Number) value).longValue();
    }
}
