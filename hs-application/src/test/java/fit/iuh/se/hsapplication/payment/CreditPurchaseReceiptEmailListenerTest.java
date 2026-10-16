package fit.iuh.se.hsapplication.payment;

import fit.iuh.se.hsbilling.entity.enums.CreditPaymentProvider;
import fit.iuh.se.hsbilling.event.CreditPurchaseCompleted;
import fit.iuh.se.hsnotification.dto.command.CreditPurchaseReceiptMailCommand;
import fit.iuh.se.hsnotification.service.EmailService;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.UserProfile;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditPurchaseReceiptEmailListenerTest {

    @Mock
    UserAccountRepository users;

    @Mock
    EmailService emailService;

    @InjectMocks
    CreditPurchaseReceiptEmailListener listener;

    @Test
    void completedPurchaseIsMappedToReceiptEmail() {
        var member = UserAccount.builder()
                .id(7L)
                .email("member@example.com")
                .profile(UserProfile.builder().displayName("Nguyễn Văn An").build())
                .build();
        when(users.findById(7L)).thenReturn(Optional.of(member));
        Instant paidAt = Instant.parse("2026-10-16T17:30:00Z");
        var event = new CreditPurchaseCompleted(
                99L, 7L, "TOKEN_10", "Gói 10 token", 10, 100_000,
                "VND", CreditPaymentProvider.PAYOS, paidAt, 14, 2, 12
        );

        listener.on(event);

        var captor = ArgumentCaptor.forClass(CreditPurchaseReceiptMailCommand.class);
        verify(emailService).sendCreditPurchaseReceiptAsync(captor.capture());
        var command = captor.getValue();
        assertEquals("member@example.com", command.recipientEmail());
        assertEquals("Nguyễn Văn An", command.displayName());
        assertEquals(99L, command.orderId());
        assertEquals("Gói 10 token", command.packageName());
        assertEquals(100_000, command.amountVnd());
        assertEquals("PAYOS", command.provider());
        assertEquals(paidAt, command.paidAt());
        assertEquals(12, command.walletAvailable());
    }

    @Test
    void missingMemberDoesNotAttemptToSendEmail() {
        when(users.findById(7L)).thenReturn(Optional.empty());
        var event = new CreditPurchaseCompleted(
                99L, 7L, "TOKEN_10", "Gói 10 token", 10, 100_000,
                "VND", CreditPaymentProvider.PAYOS, Instant.now(), 10, 0, 10
        );

        listener.on(event);

        verify(emailService, never()).sendCreditPurchaseReceiptAsync(org.mockito.ArgumentMatchers.any());
    }
}
