package fit.iuh.se.hsbilling.service;

import fit.iuh.se.hsbilling.config.CreditPaymentConfiguration;
import fit.iuh.se.hsbilling.dto.CreditWalletResponse;
import fit.iuh.se.hsbilling.entity.CreditPurchaseOrder;
import fit.iuh.se.hsbilling.entity.enums.CreditOrderStatus;
import fit.iuh.se.hsbilling.payment.CreditPaymentGateway;
import fit.iuh.se.hsbilling.payment.MockCreditPaymentGateway;
import fit.iuh.se.hsbilling.repository.CreditOrderRepository;
import fit.iuh.se.hsbilling.repository.CreditPackageRepository;
import fit.iuh.se.hsbilling.repository.CreditPaymentAggregate;
import fit.iuh.se.hsbilling.repository.CreditPaymentAttemptRepository;
import fit.iuh.se.hsbilling.service.impl.CreditPurchaseServiceImpl;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditPurchasePaymentSummaryTest {
    @Mock CreditOrderRepository orders;
    @Mock CreditPaymentAttemptRepository attempts;
    @Mock CreditPackageRepository packages;
    @Mock UserAccountRepository users;
    @Mock ConsultationCreditService credits;
    @Mock CreditPurchaseCompletionService completion;
    @Mock MockCreditPaymentGateway gateway;
    @Mock CreditPaymentConfiguration configuration;
    @Mock CreditPaymentGateway payOSGateway;
    @Mock PaymentOrderCodeAllocator orderCodes;
    @Mock TransactionTemplate transactions;
    @InjectMocks CreditPurchaseServiceImpl service;

    @Test
    void returnsOnlyTheAuthenticatedMembersSuccessfulPaymentOverview() {
        Instant from = Instant.parse("2026-09-01T00:00:00Z");
        Instant to = Instant.parse("2026-10-01T00:00:00Z");
        Instant first = Instant.parse("2026-09-03T01:00:00Z");
        Instant last = Instant.parse("2026-09-28T02:00:00Z");
        var wallet = new CreditWalletResponse(170, 20, 150);
        when(credits.getWallet(123L)).thenReturn(wallet);
        when(orders.summarizeSuccessfulPayments(123L, from, to))
                .thenReturn(new CreditPaymentAggregate(150_000, 150, 3, 1, first, last));

        var result = service.getPaymentOverview(123L, from, to);

        assertAll(
                () -> assertEquals(150_000, result.totalPaidVnd()),
                () -> assertEquals(150, result.totalPurchasedCredits()),
                () -> assertEquals(3, result.successfulOrderCount()),
                () -> assertEquals(first, result.firstPaidAt()),
                () -> assertEquals(last, result.lastPaidAt()),
                () -> assertSame(wallet, result.wallet()));
        verify(orders).summarizeSuccessfulPayments(123L, from, to);
    }

    @Test
    void rejectsInvalidOverviewPeriodBeforeReadingData() {
        Instant instant = Instant.parse("2026-09-01T00:00:00Z");

        assertThrows(AppException.class, () -> service.getPaymentOverview(123L, instant, instant));

        verifyNoInteractions(credits, orders);
    }

    @Test
    @SuppressWarnings("unchecked")
    void filtersMemberHistoryAndForcesStableNewestFirstOrdering() {
        Instant from = Instant.parse("2026-09-01T00:00:00Z");
        Instant to = Instant.parse("2026-10-01T00:00:00Z");
        var member = UserAccount.builder().id(123L).role(UserRole.MEMBER).status(AccountStatus.ACTIVE).build();
        var order = CreditPurchaseOrder.builder().id(7L).memberId(123L).packageId(5L)
                .packageCode("TOKEN_50").packageName("50 tokens").creditQuantity(50).amountVnd(50_000)
                .currency("VND").status(CreditOrderStatus.PAID).idempotencyKey("buy-7")
                .requestFingerprint("credit-order:v1:package:5").paidAt(from.plusSeconds(60)).build();
        order.setCreatedAt(from.plusSeconds(30));
        var requested = PageRequest.of(1, 20);
        var expected = PageRequest.of(1, 20,
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC,
                        "createdAt", "id"));
        when(users.findById(123L)).thenReturn(Optional.of(member));
        when(orders.findAll(any(Specification.class), eq(expected)))
                .thenReturn(new PageImpl<>(List.of(order), expected, 21));

        var result = service.getOrders(123L, CreditOrderStatus.PAID, from, to, requested);

        assertEquals(2, result.getPage());
        assertEquals(21, result.getTotalElements());
        assertEquals("7", result.getContent().getFirst().id());
        verify(orders).findAll(any(Specification.class), eq(expected));
    }
}
