package fit.iuh.se.hsapplication.nutrition;

import fit.iuh.se.hsapplication.controller.nutrition.NutritionController;
import fit.iuh.se.hsapplication.controller.nutrition.NutritionReferenceController;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;
import java.nio.file.*;
import java.util.Map;

/** THROWAWAY - serves ONLY the real nutrition controllers on a local Postgres for a browser check. Never commit. */
class TmpServeNutrition {
    @Test void serve() throws Exception {
        String stop = System.getenv("NUTRITION_SERVE_STOP");
        Assumptions.assumeTrue(stop != null);
        var ctx = new AnnotationConfigServletWebServerApplicationContext();
        ctx.register(ServeConfig.class);
        ctx.refresh();
        while (!Files.exists(Paths.get(stop))) Thread.sleep(500);
        ctx.close();
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    @Import({NutritionCatalogPostgresTest.TestConfiguration.class, NutritionController.class,
            NutritionReferenceController.class, fit.iuh.se.hsshared.advice.handler.GlobalExceptionHandler.class,
            FakeAuth.class})
    static class ServeConfig implements WebMvcConfigurer {
        @Bean DataSource dataSource() {
            return new DriverManagerDataSource("jdbc:postgresql://localhost:55432/hs", "postgres", "test");
        }
        @Bean TomcatServletWebServerFactory webServerFactory() { return new TomcatServletWebServerFactory(18080); }
        @Bean DispatcherServlet dispatcherServlet() { return new DispatcherServlet(); }
        @Override public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/**").allowedOrigins("http://localhost:5199").allowCredentials(true).allowedMethods("*");
        }
    }

    @RestController
    static class FakeAuth {
        @PostMapping("/api/auth/refresh")
        Map<String, Object> refresh() {
            return Map.of("code", 1000, "data", Map.of("accessToken", "local-preview", "tokenType", "Bearer",
                    "userSession", Map.of("userId", 1, "email", "preview.member@localhost.test",
                            "fullName", "Preview Member", "role", "MEMBER", "accountStatus", "ACTIVE")));
        }
    }
}