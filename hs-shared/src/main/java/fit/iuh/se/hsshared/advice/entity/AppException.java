package fit.iuh.se.hsshared.advice.entity;

import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.i18n.ErrorMessages;
import fit.iuh.se.hsshared.i18n.RequestLanguage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * Lỗi nghiệp vụ trả về cho client. Thông báo được dịch theo ngôn ngữ của request (header {@code lang} /
 * {@code Accept-Language}, xem RequestLanguage):
 * <ul>
 *   <li>{@code new AppException(code)}: thông báo chung của mã lỗi ({@code code.<ErrorCode>}).</li>
 *   <li>{@code AppException.of(code, "detail.<khóa>", args...)}: thông báo cụ thể có bản dịch ({@code errors*.properties}).
 *       Dùng cách này cho mọi thông báo mới.</li>
 *   <li>{@code new AppException(code, "câu cố định")}: chỉ hiện nguyên câu khi câu cùng ngôn ngữ với request; khác
 *       ngôn ngữ thì trả thông báo chung của mã lỗi (câu gốc vẫn nằm trong {@link #getMessage()} để ghi log).</li>
 * </ul>
 * {@link #getMessage()} luôn là tiếng Anh (hoặc câu gốc) để log thống nhất.
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppException extends RuntimeException {

    ErrorCode errorCode;
    /** Khóa thông báo cụ thể ({@code detail.*}); null nếu dùng thông báo chung hoặc câu cố định. */
    String messageKey;
    Object[] messageArgs;
    /** true khi tạo bằng câu cố định (constructor 2 tham số). */
    boolean customMessage;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.messageKey = null;
        this.messageArgs = null;
        this.customMessage = false;
    }

    public AppException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.messageKey = null;
        this.messageArgs = null;
        this.customMessage = true;
    }

    private AppException(ErrorCode errorCode, String messageKey, Object[] messageArgs) {
        super(ErrorMessages.detail(messageKey, messageArgs, RequestLanguage.ENGLISH));
        this.errorCode = errorCode;
        this.messageKey = messageKey;
        this.messageArgs = messageArgs;
        this.customMessage = false;
    }

    /** Lỗi với thông báo cụ thể có bản dịch, ví dụ {@code AppException.of(INVALID_PARAMETER, "detail.page-size-max", 100)}. */
    public static AppException of(ErrorCode errorCode, String messageKey, Object... args) {
        return new AppException(errorCode, messageKey, args);
    }

    /** Thông báo cho client theo ngôn ngữ {@code locale}. */
    public String localizedMessage(java.util.Locale locale) {
        if (messageKey != null) return ErrorMessages.detail(messageKey, messageArgs, locale);
        if (customMessage && ErrorMessages.isInLanguage(getMessage(), locale)) return getMessage();
        return ErrorMessages.code(errorCode, locale);
    }
}
