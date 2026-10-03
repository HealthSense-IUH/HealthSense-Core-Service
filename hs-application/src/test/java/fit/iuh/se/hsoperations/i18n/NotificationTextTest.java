package fit.iuh.se.hsoperations.i18n;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/** Bảng câu thông báo song ngữ: dịch đúng hai chiều, câu có số / trạng thái theo mẫu, câu lạ giữ nguyên. */
class NotificationTextTest {
    private static final Locale VI = Locale.forLanguageTag("vi");
    private static final Locale EN = Locale.ENGLISH;
    private static final Pattern VIETNAMESE_LETTERS = Pattern.compile(
            "[àáảãạăằắẳẵặâầấẩẫậèéẻẽẹêềếểễệìíỉĩịòóỏõọôồốổỗộơờớởỡợùúủũụưừứửữựỳýỷỹỵđ]", Pattern.CASE_INSENSITIVE);

    @Test
    void everyFixedTextHasBothLanguages() {
        for (String[] pair : NotificationText.pairs()) {
            assertFalse(VIETNAMESE_LETTERS.matcher(pair[0]).find(), "English text has Vietnamese letters: " + pair[0]);
            assertTrue(VIETNAMESE_LETTERS.matcher(pair[1]).find(), "Vietnamese text has no diacritics: " + pair[1]);
            assertEquals(pair[1], NotificationText.localize(pair[0], VI));
            assertEquals(pair[0], NotificationText.localize(pair[0], EN));
            // Thông báo cũ lưu bằng tiếng Việt vẫn đọc được bằng tiếng Anh
            assertEquals(pair[0], NotificationText.localize(pair[1], EN));
            assertEquals(pair[1], NotificationText.localize(pair[1], VI));
        }
    }

    @Test
    void templatedTextsCarryTheirValues() {
        assertEquals("Đã cộng 5 lượt tư vấn vào ví của bạn.",
                NotificationText.localize("5 consultation credits were added to your wallet.", VI));
        assertEquals("5 consultation credits were added to your wallet.",
                NotificationText.localize("5 consultation credits were added to your wallet.", EN));
        assertEquals("Đã hoàn 1 lượt tư vấn vào ví của bạn.",
                NotificationText.localize("1 consultation credit has been returned to your wallet.", VI));
        assertEquals("Trạng thái hoàn tiền của bạn: đã duyệt.",
                NotificationText.localize("Your refund status is now APPROVED.", VI));
        assertEquals("Your refund status is now review required.",
                NotificationText.localize("Your refund status is now REVIEW_REQUIRED.", EN));
        assertEquals("Trạng thái gia hạn của bạn: chờ thanh toán.",
                NotificationText.localize("Your renewal status is now WAITING_PAYMENT.", VI));
    }

    @Test
    void unknownTextIsKept() {
        assertEquals("Something new", NotificationText.localize("Something new", VI));
        assertNull(NotificationText.localize(null, VI));
    }
}
