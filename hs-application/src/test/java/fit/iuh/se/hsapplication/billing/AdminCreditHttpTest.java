package fit.iuh.se.hsapplication.billing;

import fit.iuh.se.hsapplication.config.security.*;
import fit.iuh.se.hsapplication.controller.admin.AdminCreditController;
import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hsapplication.service.ratelimit.RateLimiterService;
import fit.iuh.se.hsbilling.dto.*;
import fit.iuh.se.hsbilling.service.CreditAdministrationService;
import fit.iuh.se.hschat.service.refund.ConsultationCreditRefundService;
import fit.iuh.se.hschat.service.recovery.ConsultationCreditRecoveryService;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.*;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminCreditHttpTest {
    AnnotationConfigWebApplicationContext context; MockMvc mvc;
    @BeforeEach void start() {
        context=new AnnotationConfigWebApplicationContext(); context.setServletContext(new MockServletContext());
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
                "security.cors.allowed-origins=http://localhost:3000", "app.rate-limit.enabled=false");
        context.register(Config.class); context.refresh();
        mvc=MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }
    @AfterEach void stop(){context.close();}
    @Configuration(proxyBeanMethods=false) @EnableWebMvc
    @Import({SecurityConfig.class,AdminCreditController.class,JwtAuthenticationFilter.class,RateLimitFilter.class,
            RestAuthenticationEntryPoint.class,RestAccessDeniedHandler.class,
            fit.iuh.se.hsshared.advice.handler.GlobalExceptionHandler.class})
    static class Config {
        @Bean CreditAdministrationService admin(){return mock(CreditAdministrationService.class);}
        @Bean ConsultationCreditRefundService refunds(){return mock(ConsultationCreditRefundService.class);}
        @Bean ConsultationCreditRecoveryService recovery(){return mock(ConsultationCreditRecoveryService.class);}
        @Bean JwtDecoder decoder(){return mock(JwtDecoder.class);}
        @Bean RateLimiterService limiter(){return mock(RateLimiterService.class);}
        @Bean ObjectMapper mapper(){return JsonMapper.builder().build();}
    }
    UsernamePasswordAuthenticationToken actor(UserRole role){
        var p=UserAuthentication.builder().userId(88L).role(role).build();
        return UsernamePasswordAuthenticationToken.authenticated(p,null,List.of(new SimpleGrantedAuthority("ROLE_"+role)));
    }
    @Test void adminRoutesRejectAnonymousMemberDoctorAndCoordinator() throws Exception {
        var requests=List.of(get("/api/admin/credits/packages"),
                get("/api/admin/credits/members"),
                post("/api/admin/credits/wallets/1/adjustments").header("Idempotency-Key","x")
                        .contentType("application/json").content("{\"delta\":1,\"reason\":\"fix\"}"),
                post("/api/admin/credits/session-refunds").header("Idempotency-Key","x")
                        .contentType("application/json").content("{\"sessionId\":1,\"reason\":\"fix\"}"));
        for(var request:requests) mvc.perform(request).andExpect(status().isUnauthorized());
        for(var role:List.of(UserRole.MEMBER,UserRole.DOCTOR,UserRole.CARE_COORDINATOR))
            for(var request:requests) mvc.perform(request.with(authentication(actor(role)))).andExpect(status().isForbidden());
        verifyNoInteractions(context.getBean(CreditAdministrationService.class),context.getBean(ConsultationCreditRefundService.class));
    }
    @Test void adminAndSuperAdminReachMutationsWithPrincipalActor() throws Exception {
        for(var role:List.of(UserRole.ADMIN,UserRole.SUPER_ADMIN)) {
            mvc.perform(post("/api/admin/credits/wallets/5/adjustments").with(authentication(actor(role)))
                    .header("Idempotency-Key","adjust-"+role).contentType("application/json")
                    .content("{\"delta\":1,\"reason\":\"approved correction\"}"))
                    .andExpect(status().isOk());
        }
        verify(context.getBean(CreditAdministrationService.class)).adjust(88L,UserRole.ADMIN,5L,1L,
                "approved correction","adjust-ADMIN");
        verify(context.getBean(CreditAdministrationService.class)).adjust(88L,UserRole.SUPER_ADMIN,5L,1L,
                "approved correction","adjust-SUPER_ADMIN");
    }

    @Test void adminCanSearchPagedMemberWalletDirectory() throws Exception {
        mvc.perform(get("/api/admin/credits/members").with(authentication(actor(UserRole.ADMIN)))
                        .param("status", "ACTIVE").param("keyword", "alice")
                        .param("page", "2").param("size", "25"))
                .andExpect(status().isOk());

        verify(context.getBean(CreditAdministrationService.class)).getMembers(
                eq(88L), eq(UserRole.ADMIN), eq(AccountStatus.ACTIVE), eq("alice"),
                argThat(pageable -> pageable.getPageNumber() == 1 && pageable.getPageSize() == 25
                        && pageable.getSort().getOrderFor("createdAt") != null
                        && pageable.getSort().getOrderFor("id") != null));
    }

    @Test void memberWalletDirectoryRejectsInvalidPagination() throws Exception {
        mvc.perform(get("/api/admin/credits/members").with(authentication(actor(UserRole.ADMIN)))
                        .param("page", "0"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/admin/credits/members").with(authentication(actor(UserRole.ADMIN)))
                        .param("size", "101"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(context.getBean(CreditAdministrationService.class));
    }
}
