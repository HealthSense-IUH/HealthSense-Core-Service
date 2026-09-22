package fit.iuh.se.hsapplication.billing;

import fit.iuh.se.hsapplication.config.security.*;
import fit.iuh.se.hsapplication.controller.billing.ConsultationCreditController;
import fit.iuh.se.hsapplication.controller.billing.CreditPurchaseController;
import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hsapplication.service.ratelimit.RateLimiterService;
import fit.iuh.se.hsbilling.dto.*;
import fit.iuh.se.hsbilling.service.ConsultationCreditService;
import fit.iuh.se.hsbilling.service.CreditPurchaseService;
import fit.iuh.se.hsbilling.entity.enums.*;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.*;
import org.springframework.data.domain.*;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ConsultationCreditHttpTest {
    private AnnotationConfigWebApplicationContext context;
    private MockMvc mvc;
    private ConsultationCreditService credits;

    @BeforeEach void start() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
                "security.cors.allowed-origins=http://localhost:3000", "app.rate-limit.enabled=false");
        context.register(Config.class);
        context.refresh();
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        credits = context.getBean(ConsultationCreditService.class);
    }

    @AfterEach void stop() { if (context != null) context.close(); }

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    @Import({SecurityConfig.class, ConsultationCreditController.class, CreditPurchaseController.class, JwtAuthenticationFilter.class,
            fit.iuh.se.hsshared.advice.handler.GlobalExceptionHandler.class,
            RateLimitFilter.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})
    static class Config {
        @Bean ConsultationCreditService credits() { return mock(ConsultationCreditService.class); }
        @Bean CreditPurchaseService purchases() { return mock(CreditPurchaseService.class); }
        @Bean JwtDecoder decoder() { return mock(JwtDecoder.class); }
        @Bean RateLimiterService rateLimiter() { return mock(RateLimiterService.class); }
        @Bean ObjectMapper mapper() { return JsonMapper.builder().build(); }
    }

    private UsernamePasswordAuthenticationToken actor(UserRole role) {
        var user = UserAuthentication.builder().userId(123L).role(role).build();
        return UsernamePasswordAuthenticationToken.authenticated(user, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));
    }

    @Test void anonymousAndAllNonMemberRolesAreRejectedByActualSecurityRouting() throws Exception {
        for (String path : List.of("wallet", "packages", "ledger")) {
            mvc.perform(get("/api/credits/" + path)).andExpect(status().isUnauthorized());
            for (UserRole role : UserRole.values()) {
                if (role == UserRole.MEMBER) continue;
                mvc.perform(get("/api/credits/" + path).with(authentication(actor(role))))
                        .andExpect(status().isForbidden());
            }
        }
        verifyNoInteractions(credits);
    }

    @Test void memberIdentityAndReadContractArePreserved() throws Exception {
        when(credits.getWallet(123L)).thenReturn(new CreditWalletResponse(5, 1, 4));
        when(credits.getPackages(123L)).thenReturn(List.of(new CreditPackageResponse(
                "9007199254740993", "TEST_5", "Test-only", null, 5, 5000)));
        mvc.perform(get("/api/credits/wallet?memberId=999").with(authentication(actor(UserRole.MEMBER))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.data.available").value(4));
        mvc.perform(get("/api/credits/packages").with(authentication(actor(UserRole.MEMBER))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data[0].id").value("9007199254740993"));
        verify(credits).getWallet(123L);
        verify(credits, never()).getWallet(999L);
    }

    @Test void ledgerUsesOneBasedPaginationWithoutAcceptingAnotherMemberId() throws Exception {
        when(credits.getLedger(123L, PageRequest.of(1, 20)))
                .thenReturn(new PageResponse<>(Page.empty(PageRequest.of(1, 20))));
        mvc.perform(get("/api/credits/ledger?page=2&size=20&memberId=999").with(authentication(actor(UserRole.MEMBER))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.page").value(2));
        verify(credits).getLedger(123L, PageRequest.of(1, 20));
    }

    @Test void invalidPaginationIsBadRequestInsteadOfServerError() throws Exception {
        for (String query : List.of("page=0", "size=0", "size=101", "page=-1")) {
            mvc.perform(get("/api/credits/ledger?" + query).with(authentication(actor(UserRole.MEMBER))))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(1201));
        }
        verifyNoInteractions(credits);
    }

    @Test void purchaseApiUsesPrincipalAndOnlyPackageSelection() throws Exception {
        var purchases = context.getBean(CreditPurchaseService.class);
        var order = new CreditOrderSummary("9007199254740993", "5", "TEST_5", "Test only", 5, 5000,
                "VND", CreditOrderStatus.PAID, java.time.Instant.now(), java.time.Instant.now());
        var result = new CreditOrderResponse(order, new CreditPaymentSummary("9007199254740994",
                CreditPaymentProvider.MOCK, CreditPaymentStatus.PAID, null, null, null), new CreditWalletResponse(5, 0, 5));
        when(purchases.createOrder(123L, 5L, "buy-1")).thenReturn(result);
        mvc.perform(post("/api/credits/orders").with(authentication(actor(UserRole.MEMBER)))
                        .header("Idempotency-Key", "buy-1").contentType("application/json")
                        .content("{\"packageId\":\"5\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.order.id").value("9007199254740993"))
                .andExpect(jsonPath("$.data.order.status").value("PAID"))
                .andExpect(jsonPath("$.data.payment.provider").value("MOCK"))
                .andExpect(jsonPath("$.data.wallet.balance").value(5));
        verify(purchases).createOrder(123L, 5L, "buy-1");
    }

    @Test void purchaseRequiresKeyAndValidPackageAndMemberRole() throws Exception {
        var purchases = context.getBean(CreditPurchaseService.class);
        mvc.perform(post("/api/credits/orders").with(authentication(actor(UserRole.MEMBER)))
                .contentType("application/json").content("{\"packageId\":5}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/credits/orders").with(authentication(actor(UserRole.MEMBER)))
                .header("Idempotency-Key", "buy").contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/credits/orders").contentType("application/json").content("{}"))
                .andExpect(status().isUnauthorized());
        for (var role : UserRole.values()) {
            if (role == UserRole.MEMBER) continue;
            mvc.perform(post("/api/credits/orders").with(authentication(actor(role)))
                    .header("Idempotency-Key", "buy").contentType("application/json").content("{\"packageId\":5}"))
                    .andExpect(status().isForbidden());
            mvc.perform(get("/api/credits/orders").with(authentication(actor(role)))).andExpect(status().isForbidden());
        }
        verifyNoInteractions(purchases);
    }

    @Test void purchaseHistoryUsesMemberIdentityAndPagination() throws Exception {
        var purchases = context.getBean(CreditPurchaseService.class);
        when(purchases.getOrders(123L, PageRequest.of(0, 10))).thenReturn(new PageResponse<>(Page.empty()));
        mvc.perform(get("/api/credits/orders?memberId=999").with(authentication(actor(UserRole.MEMBER))))
                .andExpect(status().isOk());
        mvc.perform(get("/api/credits/orders/7?memberId=999").with(authentication(actor(UserRole.MEMBER))))
                .andExpect(status().isOk());
        mvc.perform(get("/api/credits/orders?size=101").with(authentication(actor(UserRole.MEMBER))))
                .andExpect(status().isBadRequest());
        verify(purchases).getOrders(123L, PageRequest.of(0, 10));
        verify(purchases).getOrder(123L, 7L);
        verifyNoMoreInteractions(purchases);
    }
}
