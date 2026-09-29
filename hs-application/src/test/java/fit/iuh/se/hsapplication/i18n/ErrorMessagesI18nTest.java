package fit.iuh.se.hsapplication.i18n;

import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.advice.handler.GlobalExceptionHandler;
import fit.iuh.se.hsshared.i18n.ErrorMessages;
import fit.iuh.se.hsshared.i18n.RequestLanguage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/** Thông báo lỗi song ngữ: bản dịch đủ và khớp, chọn ngôn ngữ theo header, handler trả đúng ngôn ngữ. */
class ErrorMessagesI18nTest {
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{(\\d+)}");

    @AfterEach
    void clearRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    private static Properties load(String resource) throws IOException {
        Properties properties = new Properties();
        try (InputStream in = ErrorMessagesI18nTest.class.getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(in, resource);
            properties.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
        return properties;
    }

    private static Set<String> placeholders(String pattern) {
        Set<String> result = new TreeSet<>();
        Matcher matcher = PLACEHOLDER.matcher(pattern);
        while (matcher.find()) result.add(matcher.group(1));
        return result;
    }

    @Test
    void englishAndVietnameseBundlesHaveTheSameKeysAndPlaceholders() throws IOException {
        for (String[] pair : new String[][]{{"i18n/errors.properties", "i18n/errors_vi.properties"},
                {"ValidationMessages.properties", "ValidationMessages_vi.properties"}}) {
            Properties en = load(pair[0]);
            Properties vi = load(pair[1]);
            assertEquals(en.stringPropertyNames(), vi.stringPropertyNames(), pair[0] + " vs " + pair[1]);
            for (String key : en.stringPropertyNames()) {
                assertEquals(placeholders(en.getProperty(key)), placeholders(vi.getProperty(key)), key);
                assertFalse(vi.getProperty(key).isBlank(), key);
            }
        }
    }

    @Test
    void everyErrorCodeHasBothLanguages() throws IOException {
        Properties en = load("i18n/errors.properties");
        Properties vi = load("i18n/errors_vi.properties");
        for (ErrorCode code : ErrorCode.values()) {
            assertNotNull(en.getProperty("code." + code.name()), code.name());
            assertNotNull(vi.getProperty("code." + code.name()), code.name());
            assertTrue(ErrorMessages.isInLanguage(vi.getProperty("code." + code.name()), RequestLanguage.VIETNAMESE),
                    "Vietnamese text expected for " + code);
        }
    }

    /** Mọi khóa detail.* và {validation.*} dùng trong code phải có trong bundle. */
    @Test
    void everyKeyUsedInSourcesExists() throws IOException {
        Properties errors = load("i18n/errors.properties");
        Properties validation = load("ValidationMessages.properties");
        Path repo = Path.of("").toAbsolutePath().getParent();
        Pattern detail = Pattern.compile("\"(detail\\.[a-z0-9-]+)\"");
        Pattern validationKey = Pattern.compile("\\{(validation\\.[a-z0-9-]+)}");
        List<String> missing = new ArrayList<>();
        int files = 0;
        try (Stream<Path> paths = Files.walk(repo)) {
            for (Path path : paths.filter(p -> p.toString().endsWith(".java") && p.toString().contains("src" + java.io.File.separator + "main")).toList()) {
                files++;
                String source = Files.readString(path);
                Matcher m = detail.matcher(source);
                while (m.find()) if (!errors.containsKey(m.group(1))) missing.add(path.getFileName() + ": " + m.group(1));
                m = validationKey.matcher(source);
                while (m.find()) if (!validation.containsKey(m.group(1))) missing.add(path.getFileName() + ": " + m.group(1));
            }
        }
        assertTrue(files > 100, "scanned " + files + " files from " + repo);
        assertEquals(List.of(), missing);
    }

    @Test
    void languageComesFromLangHeaderThenAcceptLanguageThenEnglish() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        assertEquals(RequestLanguage.ENGLISH, RequestLanguage.resolve(request));
        request.addHeader("Accept-Language", "fr-FR,vi;q=0.8,en;q=0.5");
        assertEquals(RequestLanguage.VIETNAMESE, RequestLanguage.resolve(request), "first supported language");
        request.addHeader("lang", "en");
        assertEquals(RequestLanguage.ENGLISH, RequestLanguage.resolve(request), "lang header wins");

        MockHttpServletRequest vi = new MockHttpServletRequest();
        vi.addHeader("lang", "VI-vn");
        assertEquals(RequestLanguage.VIETNAMESE, RequestLanguage.resolve(vi));
        MockHttpServletRequest unknown = new MockHttpServletRequest();
        unknown.addHeader("lang", "jp");
        unknown.addHeader("Accept-Language", "not a valid header ;;");
        assertEquals(RequestLanguage.ENGLISH, RequestLanguage.resolve(unknown));
    }

    private static void requestIn(String lang) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("lang", lang);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void handlerLocalizesCodesDetailsAndSkipsLiteralsInTheOtherLanguage() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        requestIn("vi");
        assertEquals("Không tìm thấy phiên tư vấn",
                handler.handleAppException(new AppException(ErrorCode.CONSULTATION_NOT_FOUND)).getBody().getMessage());
        assertEquals("Từ khóa tìm kiếm tối đa 100 ký tự", handler.handleAppException(
                AppException.of(ErrorCode.INVALID_PARAMETER, "detail.query-too-long", 100)).getBody().getMessage());
        assertEquals("Không đủ lượt tư vấn: cần 2, hiện có 1", handler.handleAppException(
                AppException.of(ErrorCode.INSUFFICIENT_CONSULTATION_CREDITS, "detail.insufficient-credits", 2L, 1L)).getBody().getMessage());
        // Câu cố định tiếng Anh (vd lỗi của cổng thanh toán) không hiện cho người dùng tiếng Việt: dùng thông báo của mã lỗi
        assertEquals("Cổng thanh toán gặp lỗi, vui lòng thử lại sau", handler.handleAppException(
                new AppException(ErrorCode.PAYMENT_PROVIDER_ERROR, "PayOS timeout")).getBody().getMessage());
        assertEquals("Không tìm thấy tài nguyên", handler.handleNoResourceFound(null).getBody().getMessage());

        requestIn("en");
        assertEquals("Consultation session not found",
                handler.handleAppException(new AppException(ErrorCode.CONSULTATION_NOT_FOUND)).getBody().getMessage());
        assertEquals("Search text must be at most 100 characters", handler.handleAppException(
                AppException.of(ErrorCode.INVALID_PARAMETER, "detail.query-too-long", 100)).getBody().getMessage());
        assertEquals("PayOS timeout", handler.handleAppException(
                new AppException(ErrorCode.PAYMENT_PROVIDER_ERROR, "PayOS timeout")).getBody().getMessage());
        // Thông báo có dấu nháy đơn (MessageFormat) hiện đúng
        assertEquals("Idempotency-Key must contain 1-128 ASCII letters, digits, '.', '_', ':' or '-'", handler.handleAppException(
                AppException.of(ErrorCode.INVALID_PARAMETER, "detail.idempotency-key-format")).getBody().getMessage());
    }

    @Test
    void exceptionMessageStaysEnglishForLogs() {
        requestIn("vi");
        AppException exception = AppException.of(ErrorCode.INVALID_PARAMETER, "detail.note-too-long", 1000);
        assertEquals("Note must be at most 1000 characters", exception.getMessage());
        assertEquals("Ghi chú tối đa 1000 ký tự", exception.localizedMessage(RequestLanguage.VIETNAMESE));
    }
}
