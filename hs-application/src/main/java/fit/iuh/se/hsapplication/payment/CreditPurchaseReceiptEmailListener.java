package fit.iuh.se.hsapplication.payment;

import fit.iuh.se.hsbilling.event.CreditPurchaseCompleted;
import fit.iuh.se.hsnotification.dto.command.CreditPurchaseReceiptMailCommand;
import fit.iuh.se.hsnotification.service.EmailService;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreditPurchaseReceiptEmailListener {

    private final UserAccountRepository users;
    private final EmailService emailService;

    @EventListener
    public void on(CreditPurchaseCompleted event) {
        var member = users.findById(event.memberId()).orElse(null);
        if (member == null || member.getEmail() == null || member.getEmail().isBlank()) {
            log.warn("Cannot send credit purchase receipt for order {}: member {} has no email",
                    event.orderId(), event.memberId());
            return;
        }
        String displayName = member.getProfile() == null ? null : member.getProfile().getDisplayName();
        emailService.sendCreditPurchaseReceiptAsync(new CreditPurchaseReceiptMailCommand(
                member.getEmail(),
                displayName,
                event.orderId(),
                event.packageCode(),
                event.packageName(),
                event.creditQuantity(),
                event.amountVnd(),
                event.currency(),
                event.provider().name(),
                event.paidAt(),
                event.walletBalance(),
                event.walletReserved(),
                event.walletAvailable()
        ));
    }
}
