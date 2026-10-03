package fit.iuh.se.hsshared.i18n;

import java.util.Locale;

/**
 * Chọn bản tiếng Việt / tiếng Anh của một nội dung lưu song song trong DB (cột {@code x} và {@code x_en}).
 * Request tiếng Anh mà chưa có bản tiếng Anh thì trả bản tiếng Việt, không để trống.
 */
public final class LocalizedText {
    private LocalizedText() {}

    /** Theo ngôn ngữ của request đang xử lý ({@link RequestLanguage#current()}). */
    public static String pick(String vietnamese, String english) {
        return pick(vietnamese, english, RequestLanguage.current());
    }

    public static String pick(String vietnamese, String english, Locale locale) {
        boolean useEnglish = !RequestLanguage.isVietnamese(locale) && english != null && !english.isBlank();
        return useEnglish ? english : vietnamese;
    }
}
