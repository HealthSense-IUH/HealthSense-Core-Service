package fit.iuh.se.hsbilling.service;

import cn.hutool.core.lang.Snowflake;
import fit.iuh.se.hsbilling.entity.CreditWallet;
import fit.iuh.se.hsbilling.repository.*;
import fit.iuh.se.hsbilling.service.impl.CreditAdministrationServiceImpl;
import fit.iuh.se.hsoperations.event.OperationalEventPublisher;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.UserProfile;
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
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditAdministrationMemberDirectoryTest {
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
    void listsMembersWithWalletAndZeroSnapshotWithoutCreatingMissingWallet() {
        var pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        UserAccount funded = member(101L, "funded@gmail.com", "Funded Member", "0901");
        UserAccount empty = member(102L, "empty@gmail.com", "Empty Member", null);
        when(users.findUsers(UserRole.MEMBER, null, 88L, pageable))
                .thenReturn(new PageImpl<>(List.of(funded, empty), pageable, 2));
        CreditWallet wallet = CreditWallet.builder().id(201L).memberId(101L).balance(7).reserved(2).version(3L).build();
        wallet.setUpdatedAt(Instant.parse("2026-09-23T08:30:00Z"));
        when(wallets.findAllByMemberIdIn(anyCollection())).thenReturn(List.of(wallet));

        var response = service.getMembers(88L, UserRole.ADMIN, null, null, pageable);

        assertEquals(2, response.getContent().size());
        var fundedResult = response.getContent().get(0);
        assertEquals("101", fundedResult.memberId());
        assertTrue(fundedResult.walletInitialized());
        assertEquals(7, fundedResult.balance());
        assertEquals(2, fundedResult.reserved());
        assertEquals(5, fundedResult.available());
        var emptyResult = response.getContent().get(1);
        assertEquals("102", emptyResult.memberId());
        assertFalse(emptyResult.walletInitialized());
        assertEquals(0, emptyResult.balance());
        assertEquals(0, emptyResult.reserved());
        assertEquals(0, emptyResult.available());
        assertNull(emptyResult.walletUpdatedAt());
        verify(wallets).findAllByMemberIdIn(argThat(ids -> ids.containsAll(List.of(101L, 102L))));
        verify(wallets, never()).initialize(anyLong(), anyLong());
        verify(wallets, never()).save(any());
    }

    @Test
    void trimsKeywordAndSearchesMemberDirectory() {
        var pageable = PageRequest.of(0, 20);
        when(users.searchUsers(UserRole.MEMBER, AccountStatus.ACTIVE, 88L, "Alice", pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        var response = service.getMembers(88L, UserRole.SUPER_ADMIN, AccountStatus.ACTIVE, "  Alice  ", pageable);

        assertTrue(response.getContent().isEmpty());
        verify(users, never()).findUsers(any(), any(), anyLong(), any());
        verify(wallets, never()).findAllByMemberIdIn(anyCollection());
    }

    @Test
    void rejectsNonAdminBeforeReadingMembers() {
        var pageable = PageRequest.of(0, 20);

        assertThrows(AppException.class,
                () -> service.getMembers(88L, UserRole.MEMBER, null, null, pageable));

        verifyNoInteractions(users, wallets);
    }

    private UserAccount member(Long id, String email, String displayName, String phone) {
        UserAccount user = UserAccount.builder().id(id).email(email).passwordHash("hash")
                .role(UserRole.MEMBER).status(AccountStatus.ACTIVE).build();
        UserProfile profile = UserProfile.builder().id(id + 1000).user(user)
                .displayName(displayName).phone(phone).build();
        user.setProfile(profile);
        return user;
    }
}
