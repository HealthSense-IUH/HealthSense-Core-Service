package fit.iuh.se.hsnotification.service;

import fit.iuh.se.hsnotification.dto.command.CreditPurchaseReceiptMailCommand;

public interface EmailService {

    void sendForgotPasswordOtpAsync(String email, String otp);

    void sendTemporaryPasswordAsync(String email, String displayName, String role, String temporaryPassword);

    void sendCreditPurchaseReceiptAsync(CreditPurchaseReceiptMailCommand command);
}
