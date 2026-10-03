package fit.iuh.se.hsapplication.nutrition;

import fit.iuh.se.hsshared.i18n.RequestLanguage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Giả một request có header {@code lang} cho test gọi thẳng service (ngoài request thì mặc định là tiếng Anh).
 * Nhớ gọi {@link #clear()} sau mỗi test.
 */
final class RequestLanguageScope {
    private RequestLanguageScope() {}

    static void use(String lang) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RequestLanguage.HEADER, lang);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    static void clear() {
        RequestContextHolder.resetRequestAttributes();
    }
}
