package fit.iuh.se.hsapplication.advice;

import fit.iuh.se.hsshared.advice.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void missingRouteReturnsNotFoundInsteadOfUncategorizedServerError() {
        var response = handler.handleNoResourceFound(
                new NoResourceFoundException(HttpMethod.GET, "api/missing", "/api/missing"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getCode());
        assertEquals("Resource not found", response.getBody().getMessage());
    }
}
