package fit.iuh.se.hsbilling.service;

import cn.hutool.core.lang.Snowflake;
import fit.iuh.se.hsbilling.dto.CreditWalletResponse;
import fit.iuh.se.hsbilling.entity.CreditLedgerEntry;
import fit.iuh.se.hsbilling.entity.CreditWallet;
import fit.iuh.se.hsbilling.entity.enums.CreditOperation;
import fit.iuh.se.hsbilling.entity.enums.CreditSourceType;
import fit.iuh.se.hsbilling.repository.*;
import fit.iuh.se.hsbilling.service.impl.ConsultationCreditServiceImpl;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.enums.*;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageRequest;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsultationCreditReadTest {
    private final CreditWalletRepository wallets = mock(CreditWalletRepository.class);
    private final CreditLedgerRepository ledger = mock(CreditLedgerRepository.class);
    private final CreditReservationRepository reservations = mock(CreditReservationRepository.class);
    private final CreditPackageRepository packages = mock(CreditPackageRepository.class);
    private final UserAccountRepository users = mock(UserAccountRepository.class);
    private final ConsultationCreditService credits = new ConsultationCreditServiceImpl(
            wallets, ledger, reservations, packages, users, new Snowflake(24, 24));

    @Test void newMemberReadsZeroWithoutWriting() {
        when(users.findById(1L)).thenReturn(Optional.of(UserAccount.builder()
                .id(1L).role(UserRole.MEMBER).status(AccountStatus.ACTIVE).build()));
        assertEquals(new CreditWalletResponse(0, 0, 0), credits.getWallet(1L));
        assertEquals(0, credits.getLedger(1L, PageRequest.of(0, 10)).getTotalElements());
        verify(wallets, times(2)).findByMemberId(1L);
        verifyNoMoreInteractions(wallets);
        verifyNoInteractions(ledger, reservations);
    }

    @Test void serviceAuthorizationRejectsInactiveAndOtherRolesBeforeReadingBilling() {
        var account = UserAccount.builder().id(1L).role(UserRole.MEMBER).status(AccountStatus.INACTIVE).build();
        when(users.findById(1L)).thenReturn(Optional.of(account));
        assertEquals(ErrorCode.ACCOUNT_DISABLED, assertThrows(AppException.class, () -> credits.getWallet(1L)).getErrorCode());
        account.setStatus(AccountStatus.ACTIVE);
        for (var role : UserRole.values()) {
            if (role == UserRole.MEMBER) continue;
            account.setRole(role);
            assertEquals(ErrorCode.ACCESS_DENIED, assertThrows(AppException.class, () -> credits.getPackages(1L)).getErrorCode());
        }
        verifyNoInteractions(wallets, ledger, reservations, packages);
    }

    @Test void admissionChecksAvailabilityWithoutMutatingWalletOrLedger() {
        activeMember(1L);
        var wallet = CreditWallet.builder().id(10L).memberId(1L).balance(3L).reserved(2L).version(0L).build();
        when(wallets.findByMemberIdForUpdate(1L)).thenReturn(Optional.of(wallet));

        credits.requireAvailable(1L, 1L);

        assertEquals(3L, wallet.getBalance());
        assertEquals(2L, wallet.getReserved());
        verify(wallets, never()).saveAndFlush(any());
        verifyNoInteractions(ledger, reservations);
    }

    @Test void sessionChargeDeductsBalanceWithoutChangingReservedAndIsRecordedOnce() {
        activeMember(1L);
        var wallet = CreditWallet.builder().id(10L).memberId(1L).balance(5L).reserved(2L).version(0L).build();
        when(wallets.findByMemberIdForUpdate(1L)).thenReturn(Optional.of(wallet));
        when(ledger.findByIdempotencyKey("credit:session-charge:20")).thenReturn(Optional.empty());
        when(ledger.findByOperationAndSourceTypeAndSourceId(
                CreditOperation.SESSION_CHARGE, CreditSourceType.CONSULTATION_SESSION, 30L))
                .thenReturn(Optional.empty());

        assertEquals(new CreditWalletResponse(4L, 2L, 2L), credits.chargeSession(1L, 20L, 30L, 1L));

        ArgumentCaptor<CreditLedgerEntry> entry = ArgumentCaptor.forClass(CreditLedgerEntry.class);
        verify(ledger).save(entry.capture());
        assertEquals(CreditOperation.SESSION_CHARGE, entry.getValue().getOperation());
        assertEquals(-1L, entry.getValue().getDeltaBalance());
        assertEquals(0L, entry.getValue().getDeltaReserved());
        assertEquals(4L, entry.getValue().getBalanceAfter());
        assertEquals(2L, entry.getValue().getReservedAfter());
    }

    @Test void admissionAndConfirmationBothRejectWhenAvailableCreditIsGone() {
        activeMember(1L);
        var wallet = CreditWallet.builder().id(10L).memberId(1L).balance(2L).reserved(2L).version(0L).build();
        when(wallets.findByMemberIdForUpdate(1L)).thenReturn(Optional.of(wallet));

        assertEquals(ErrorCode.INSUFFICIENT_CONSULTATION_CREDITS,
                assertThrows(AppException.class, () -> credits.requireAvailable(1L, 1L)).getErrorCode());
        assertEquals(ErrorCode.INSUFFICIENT_CONSULTATION_CREDITS,
                assertThrows(AppException.class, () -> credits.chargeSession(1L, 20L, 30L, 1L)).getErrorCode());
        assertEquals(2L, wallet.getBalance());
        assertEquals(2L, wallet.getReserved());
    }

    private void activeMember(Long id) {
        when(users.findById(id)).thenReturn(Optional.of(UserAccount.builder()
                .id(id).role(UserRole.MEMBER).status(AccountStatus.ACTIVE).build()));
    }
}
