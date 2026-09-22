package fit.iuh.se.hsbilling.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentOrderCodeAllocator {
    private final EntityManager entityManager;

    public long next() {
        return ((Number) entityManager.createNativeQuery("select nextval('payment_order_code_seq')")
                .getSingleResult()).longValue();
    }
}
