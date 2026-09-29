package fit.iuh.se.hsapplication.i18n;

import fit.iuh.se.hsapplication.config.LocaleConfig;
import fit.iuh.se.hsapplication.config.security.RestAuthenticationEntryPoint;
import fit.iuh.se.hsauth.dto.request.LoginRequest;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.advice.handler.GlobalExceptionHandler;
import jakarta.validation.Valid;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.json.JsonMapper;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Frontend gửi header {@code lang: vi | en}; mọi loại lỗi trả thông báo đúng ngôn ngữ đó. */
class LocalizedErrorsHttpTest {
    private MockMvc mvc;
    private Locale jvmDefault;

    @RestController
    static class ProbeController {
        @PostMapping("/probe/login")
        void login(@Valid @RequestBody LoginRequest request) {
        }

        @GetMapping("/probe/page")
        void page(@RequestParam int size) {
            if (size > 100) throw AppException.of(ErrorCode.INVALID_PARAMETER, "detail.page-size-max", 100);
        }
    }

    @BeforeEach
    void setUp() {
        // Máy chủ đặt tiếng Việt vẫn phải trả tiếng Anh khi Frontend gửi lang: en
        jvmDefault = Locale.getDefault();
        Locale.setDefault(Locale.forLanguageTag("vi-VN"));
        mvc = MockMvcBuilders.standaloneSetup(new ProbeController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setLocaleResolver(new LocaleConfig().localeResolver())
                .build();
    }

    @AfterEach
    void restoreLocale() {
        Locale.setDefault(jvmDefault);
    }

    @Test
    void validationMessagesFollowTheLangHeader() throws Exception {
        String body = "{\"email\":\"not-an-email\",\"password\":\"x\"}";
        mvc.perform(post("/probe/login").header("lang", "vi").contentType("application/json").content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email không hợp lệ"));
        mvc.perform(post("/probe/login").header("lang", "en").contentType("application/json").content(body))
                .andExpect(jsonPath("$.message").value("Email is invalid"));
        mvc.perform(post("/probe/login").header("Accept-Language", "vi-VN,vi;q=0.9").contentType("application/json")
                        .content("{\"password\":\"x\"}"))
                .andExpect(jsonPath("$.message").value("Email là bắt buộc"));
    }

    @Test
    void appExceptionsAndTypeErrorsFollowTheLangHeader() throws Exception {
        mvc.perform(get("/probe/page").param("size", "500").header("lang", "vi"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Kích thước trang phải từ 1 đến 100"));
        mvc.perform(get("/probe/page").param("size", "500").header("lang", "en"))
                .andExpect(jsonPath("$.message").value("Page size must be between 1 and 100"));
        mvc.perform(get("/probe/page").param("size", "abc").header("lang", "vi"))
                .andExpect(jsonPath("$.message").value("size phải có kiểu int"));
    }

    @Test
    void securityErrorsWrittenOutsideSpringMvcAreLocalizedToo() throws Exception {
        var entryPoint = new RestAuthenticationEntryPoint(JsonMapper.builder().build());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("lang", "vi");
        MockHttpServletResponse response = new MockHttpServletResponse();
        entryPoint.commence(request, response, new InsufficientAuthenticationException("no token"));
        String json = response.getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(json.contains("Bạn chưa đăng nhập hoặc phiên đăng nhập đã hết hạn"), json);
    }
}
