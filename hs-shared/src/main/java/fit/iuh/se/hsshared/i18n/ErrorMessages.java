package fit.iuh.se.hsshared.i18n;

import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * Thông báo lỗi song ngữ, đọc từ {@code i18n/errors.properties} (tiếng Anh, mặc định) và
 * {@code i18n/errors_vi.properties} (tiếng Việt), UTF-8.
 * <ul>
 *   <li>{@code code.<ErrorCode>}: thông báo chung của mã lỗi.</li>
 *   <li>{@code detail.<khóa>}: thông báo cụ thể, tham số dạng {@code {0}}, {@code {1}} (MessageFormat).</li>
 * </ul>
 * Khóa thiếu ở tiếng Việt thì lấy tiếng Anh; thiếu cả hai thì dùng thông báo gốc trong ErrorCode / chính khóa.
 * Không phụ thuộc Spring nên dùng được cả trong filter bảo mật.
 */
public final class ErrorMessages {
    static final String BUNDLE = "i18n/errors";
    private static final ResourceBundle.Control CONTROL =
            ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES);
    /** Chữ cái có dấu tiếng Việt, để biết một câu có đang là tiếng Việt không. */
    private static final Pattern VIETNAMESE_LETTERS = Pattern.compile(
            "[àáảãạăằắẳẵặâầấẩẫậèéẻẽẹêềếểễệìíỉĩịòóỏõọôồốổỗộơờớởỡợùúủũụưừứửữựỳýỷỹỵđ]", Pattern.CASE_INSENSITIVE);

    private ErrorMessages() {}

    public static String code(ErrorCode errorCode, Locale locale) {
        String text = find("code." + errorCode.name(), locale);
        return text != null ? text : errorCode.getMessage();
    }

    public static String detail(String key, Object[] args, Locale locale) {
        String pattern = find(key, locale);
        if (pattern == null) return key;
        Object[] safeArgs = args == null ? new Object[0] : Arrays.stream(args).map(String::valueOf).toArray();
        // Tham số đưa vào dạng chuỗi để MessageFormat không tự định dạng số theo locale (1000 -> "1.000")
        return new MessageFormat(pattern, locale).format(safeArgs);
    }

    public static boolean has(String key) {
        return find(key, RequestLanguage.ENGLISH) != null;
    }

    /** Câu có dấu tiếng Việt thì coi là tiếng Việt; câu không dấu coi là tiếng Anh. */
    public static boolean isInLanguage(String text, Locale locale) {
        boolean vietnamese = text != null && VIETNAMESE_LETTERS.matcher(text).find();
        return vietnamese == RequestLanguage.isVietnamese(locale);
    }

    /** Các ngôn ngữ có bộ thông báo, để kiểm thử độ đủ của bản dịch. */
    public static List<Locale> supportedLocales() {
        return List.of(RequestLanguage.ENGLISH, RequestLanguage.VIETNAMESE);
    }

    static ResourceBundle bundle(Locale locale) {
        return ResourceBundle.getBundle(BUNDLE, locale, CONTROL);
    }

    private static String find(String key, Locale locale) {
        try {
            ResourceBundle bundle = bundle(locale == null ? RequestLanguage.DEFAULT : locale);
            return bundle.containsKey(key) ? bundle.getString(key) : null;
        } catch (MissingResourceException e) {
            return null;
        }
    }
}
