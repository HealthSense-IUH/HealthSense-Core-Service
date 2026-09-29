package fit.iuh.se.hsshared.i18n;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

/**
 * LocaleResolver của Spring MVC theo header {@code lang} / {@code Accept-Language} (xem {@link RequestLanguage}), để
 * thông báo Bean Validation ({@code ValidationMessages*.properties}) cũng dịch theo ngôn ngữ Frontend gửi lên.
 * Ngôn ngữ do từng request quyết định nên không hỗ trợ đổi locale phía server.
 */
public class LangHeaderLocaleResolver implements LocaleResolver {
    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        return RequestLanguage.resolve(request);
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        throw new UnsupportedOperationException("Language is chosen per request via the 'lang' header");
    }
}
