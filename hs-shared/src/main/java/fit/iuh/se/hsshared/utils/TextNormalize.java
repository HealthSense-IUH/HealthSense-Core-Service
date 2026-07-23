package fit.iuh.se.hsshared.utils;

import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;

import java.util.Locale;

/**
 * @author : user664dntp
 * @mailto : phatdang19052004@gmail.com
 * @created : 23/07/2026, Thursday
 **/
public class TextNormalize {
    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public static String requireText(String value, String message) {
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_ARGUMENT, message);
        }
        return normalized;
    }
}
