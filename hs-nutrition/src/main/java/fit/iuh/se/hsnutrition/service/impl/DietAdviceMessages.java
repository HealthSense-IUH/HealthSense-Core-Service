package fit.iuh.se.hsnutrition.service.impl;

import fit.iuh.se.hsshared.i18n.RequestLanguage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Câu giải thích màu đánh giá của {@link DietAdvisor}, song ngữ: {@code i18n/diet_advice.properties} (tiếng Anh, mặc
 * định) và {@code i18n/diet_advice_vi.properties} (tiếng Việt), UTF-8, tham số dạng {@code {0}} (MessageFormat).
 */
final class DietAdviceMessages {
    static final String BUNDLE = "i18n/diet_advice";
    private static final ResourceBundle.Control CONTROL =
            ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES);

    private final Locale locale;
    private final ResourceBundle bundle;

    DietAdviceMessages(Locale locale) {
        this.locale = RequestLanguage.isVietnamese(locale) ? RequestLanguage.VIETNAMESE : RequestLanguage.ENGLISH;
        this.bundle = ResourceBundle.getBundle(BUNDLE, this.locale, CONTROL);
    }

    String get(String key, Object... args) {
        Object[] text = Arrays.stream(args).map(String::valueOf).toArray();
        // Tham số đã là chuỗi (số định dạng sẵn bằng number()) để MessageFormat không tự định dạng lại
        return new MessageFormat(bundle.getString(key), locale).format(text);
    }

    /** Số tối đa 2 chữ số thập phân theo ngôn ngữ: 482.9 -> "482,9" (vi) / "482.9" (en), 600.0 -> "600". */
    String number(double value) {
        NumberFormat format = NumberFormat.getNumberInstance(locale);
        format.setMaximumFractionDigits(2);
        format.setGroupingUsed(false);
        return format.format(BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP));
    }

    /** Các ngôn ngữ có bộ câu, để kiểm thử độ đủ của bản dịch. */
    static List<Locale> supportedLocales() {
        return List.of(RequestLanguage.ENGLISH, RequestLanguage.VIETNAMESE);
    }

    static ResourceBundle bundle(Locale locale) {
        return ResourceBundle.getBundle(BUNDLE, locale, CONTROL);
    }
}
