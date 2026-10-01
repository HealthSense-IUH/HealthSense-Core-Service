package fit.iuh.se.hsbilling.service;

import cn.hutool.core.lang.Snowflake;
import fit.iuh.se.hsbilling.repository.*;
import fit.iuh.se.hsbilling.service.impl.CreditAdministrationServiceImpl;
import fit.iuh.se.hsoperations.event.OperationalEventPublisher;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditAdministrationPaymentOverviewTest {
    @Mock CreditPackageRepository packages;
    @Mock CreditWalletRepository wallets;
    @Mock CreditLedgerRepository ledger;
    @Mock CreditReservationRepository reservations;
    @Mock CreditOrderRepository orders;
    @Mock CreditPaymentAttemptRepository attempts;
    @Mock UserAccountRepository users;
    @Mock Snowflake ids;
    @Mock OperationalEventPublisher events;
    @InjectMocks CreditAdministrationServiceImpl service;

    @Test
    void returnsSuccessfulPaymentOverviewForRequestedPeriod() {
        Instant from = Instant.parse("2026-09-01T00:00:00Z");
        Instant to = Instant.parse("2026-10-01T00:00:00Z");
        Instant first = Instant.parse("2026-09-03T01:00:00Z");
        Instant last = Instant.parse("2026-09-28T02:00:00Z");
        CreditPaymentAggregate aggregate = new CreditPaymentAggregate(1_500_000, 150, 12, 8, first, last);
        when(orders.summarizeSuccessfulPayments(null, from, to)).thenReturn(aggregate);

        var result = service.getPaymentOverview(UserRole.ADMIN, null, from, to);

        assertEquals(1_500_000, result.totalPaidVnd());
        assertEquals(150, result.totalPurchasedCredits());
        assertEquals(12, result.successfulOrderCount());
        assertEquals(8, result.payingMemberCount());
        assertEquals(first, result.firstPaidAt());
        assertEquals(last, result.lastPaidAt());
    }

    @Test
    void verifiesMemberBeforeReturningMemberOverview() {
        UserAccount member = UserAccount.builder().id(101L).role(UserRole.MEMBER).build();
        when(users.findById(101L)).thenReturn(Optional.of(member));
        CreditPaymentAggregate aggregate = new CreditPaymentAggregate(0, 0, 0, 0, null, null);
        when(orders.summarizeSuccessfulPayments(101L, null, null)).thenReturn(aggregate);

        var result = service.getPaymentOverview(UserRole.SUPER_ADMIN, 101L, null, null);

        assertEquals(0, result.totalPaidVnd());
        assertEquals(0, result.totalPurchasedCredits());
        assertEquals(0, result.successfulOrderCount());
        assertEquals(0, result.payingMemberCount());
    }

    @Test
    void rejectsInvalidPeriodWithoutQueryingOrders() {
        Instant instant = Instant.parse("2026-09-01T00:00:00Z");

        assertThrows(AppException.class,
                () -> service.getPaymentOverview(UserRole.ADMIN, null, instant, instant));

        verifyNoInteractions(orders);
    }

    @Test
    void rejectsNonAdminWithoutQueryingOrders() {
        assertThrows(AppException.class,
                () -> service.getPaymentOverview(UserRole.MEMBER, null, null, null));

        verifyNoInteractions(orders);
    }
}
