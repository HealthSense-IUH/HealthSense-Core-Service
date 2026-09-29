package fit.iuh.se.hsapplication.config;

import fit.iuh.se.hsshared.i18n.LangHeaderLocaleResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;

/**
 * Ngôn ngữ của mỗi request lấy từ header {@code lang} (vi | en), sau đó {@code Accept-Language}; mặc định tiếng Anh.
 * Dùng cho thông báo lỗi (i18n/errors*.properties) và thông báo kiểm tra dữ liệu (ValidationMessages*.properties).
 */
@Configuration
public class LocaleConfig {
    /** Tên bean phải là "localeResolver" để DispatcherServlet dùng. */
    @Bean
    public LocaleResolver localeResolver() {
        return new LangHeaderLocaleResolver();
    }
}
