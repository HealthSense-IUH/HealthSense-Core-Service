package fit.iuh.se.hsapplication.config.security;

import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    ObjectMapper objectMapper;

    @Override
    public void commence(
            @NonNull HttpServletRequest request,
            HttpServletResponse response,
            @NonNull AuthenticationException authException) throws IOException, ServletException {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        objectMapper.writeValue(response.getWriter(), apiResponse);
        response.flushBuffer();
    }
}
