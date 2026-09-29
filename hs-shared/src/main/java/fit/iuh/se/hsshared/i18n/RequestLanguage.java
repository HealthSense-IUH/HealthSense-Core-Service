package fit.iuh.se.hsshared.i18n;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Locale;

/**
 * Ngôn ngữ của request để trả thông báo lỗi: tiếng Việt hoặc tiếng Anh.
 * <ol>
 *   <li>Header {@code lang}: {@code vi} | {@code en} (Frontend gửi theo ngôn ngữ react-i18next đang chọn).</li>
 *   <li>Header chuẩn {@code Accept-Language} (vd {@code vi-VN,vi;q=0.9,en;q=0.8}): ngôn ngữ hỗ trợ đầu tiên.</li>
 *   <li>Không có hoặc không hỗ trợ: tiếng Anh, giữ nguyên hành vi cũ cho client chưa gửi header.</li>
 * </ol>
 */
public final class RequestLanguage {
    public static final String HEADER = "lang";
    public static final Locale VIETNAMESE = Locale.forLanguageTag("vi");
    public static final Locale ENGLISH = Locale.ENGLISH;
    public static final Locale DEFAULT = ENGLISH;

    private RequestLanguage() {}

    public static Locale resolve(HttpServletRequest request) {
        if (request == null) return DEFAULT;
        Locale fromLang = parse(request.getHeader(HEADER));
        if (fromLang != null) return fromLang;
        String acceptLanguage = request.getHeader("Accept-Language");
        if (acceptLanguage != null && !acceptLanguage.isBlank()) {
            try {
                for (Locale.LanguageRange range : Locale.LanguageRange.parse(acceptLanguage)) {
                    Locale supported = parse(range.getRange());
                    if (supported != null) return supported;
                }
            } catch (IllegalArgumentException ignored) {
                // Accept-Language sai cú pháp: dùng mặc định
            }
        }
        return DEFAULT;
    }

    /** Ngôn ngữ của request đang xử lý (ngoài request, ví dụ job nền: mặc định). */
    public static Locale current() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return attributes instanceof ServletRequestAttributes servlet ? resolve(servlet.getRequest()) : DEFAULT;
    }

    /** "vi", "vi-VN", "VI" -> tiếng Việt; "en", "en-US" -> tiếng Anh; còn lại null. */
    static Locale parse(String value) {
        if (value == null) return null;
        String language = value.trim().toLowerCase(Locale.ROOT);
        if (language.startsWith("vi")) return VIETNAMESE;
        if (language.startsWith("en")) return ENGLISH;
        return null;
    }

    public static boolean isVietnamese(Locale locale) {
        return locale != null && "vi".equals(locale.getLanguage());
    }
}
