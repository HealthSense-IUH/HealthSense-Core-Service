package fit.iuh.se.hsapplication.assistant;

import fit.iuh.se.hsapplication.config.security.JwtAuthenticationFilter;
import fit.iuh.se.hsapplication.config.security.RateLimitFilter;
import fit.iuh.se.hsapplication.config.security.RestAccessDeniedHandler;
import fit.iuh.se.hsapplication.config.security.RestAuthenticationEntryPoint;
import fit.iuh.se.hsapplication.config.security.SecurityConfig;
import fit.iuh.se.hsapplication.controller.assistant.AssistantController;
import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hsassistant.dto.AssistantConversationResponse;
import fit.iuh.se.hsassistant.dto.AssistantUser;
import fit.iuh.se.hsassistant.service.AssistantChatService;
import fit.iuh.se.hsassistant.service.AssistantConversationService;
import fit.iuh.se.hsapplication.service.ratelimit.RateLimiterService;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import reactor.core.publisher.Flux;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Kiểm tra lớp HTTP của trợ lý: bảo vệ bằng JWT, tạo hội thoại, và luồng SSE trả từng mẩu chữ
 * (AssistantChatService được mock, không gọi OpenAI thật).
 */
class AssistantControllerHttpTest {

	private AnnotationConfigWebApplicationContext context;
	private MockMvc mvc;

	@BeforeEach
	void start() {
		context = new AnnotationConfigWebApplicationContext();
		context.setServletContext(new MockServletContext());
		TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
				"security.cors.allowed-origins=http://localhost:3000", "app.rate-limit.enabled=false");
		context.register(Config.class);
		context.refresh();
		mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
	}

	@AfterEach
	void stop() {
		context.close();
	}

	@Configuration(proxyBeanMethods = false)
	@EnableWebMvc
	@Import({SecurityConfig.class, AssistantController.class, JwtAuthenticationFilter.class,
			RateLimitFilter.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class,
			fit.iuh.se.hsshared.advice.handler.GlobalExceptionHandler.class})
	static class Config {
		@Bean
		AssistantConversationService conversationService() {
			return mock(AssistantConversationService.class);
		}

		@Bean
		AssistantChatService chatService() {
			return mock(AssistantChatService.class);
		}

		@Bean
		JwtDecoder decoder() {
			return mock(JwtDecoder.class);
		}

		@Bean
		RateLimiterService limiter() {
			return mock(RateLimiterService.class);
		}

		@Bean
		ObjectMapper mapper() {
			return JsonMapper.builder().build();
		}
	}

	private static Authentication actor(UserRole role) {
		UserAuthentication principal = new UserAuthentication(123L, "member@test.local", role);
		return new UsernamePasswordAuthenticationToken(principal, null,
				List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));
	}

	@Test
	void rejectsAnonymous() throws Exception {
		mvc.perform(get("/api/assistant/conversations")).andExpect(status().isUnauthorized());
		mvc.perform(post("/api/assistant/conversations")).andExpect(status().isUnauthorized());
	}

	@Test
	void createsConversationForCurrentUser() throws Exception {
		var created = new AssistantConversationResponse("c-1", null, Instant.now(), Instant.now());
		when(context.getBean(AssistantConversationService.class).create(123L)).thenReturn(created);

		mvc.perform(post("/api/assistant/conversations").with(authentication(actor(UserRole.MEMBER))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.id").value("c-1"));

		verify(context.getBean(AssistantConversationService.class)).create(123L);
	}

	@Test
	void streamsAnswerAsServerSentEvents() throws Exception {
		AssistantChatService chat = context.getBean(AssistantChatService.class);
		when(chat.stream(any(AssistantUser.class), eq("c-1"), eq("Xin chào")))
				.thenReturn(Flux.just("Chào ", "bạn"));

		MvcResult started = mvc.perform(post("/api/assistant/conversations/c-1/stream")
						.with(authentication(actor(UserRole.DOCTOR)))
						.header("lang", "vi")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"message\":\"Xin chào\"}"))
				.andExpect(request().asyncStarted())
				.andReturn();

		String body = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
						.asyncDispatch(started))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);

		assertThat(body).contains("event:delta").contains("\"text\":\"Chào \"").contains("\"text\":\"bạn\"")
				.contains("event:done");
	}

	@Test
	void streamFinishesCleanlyWhenAuthenticatedByBearerToken() throws Exception {
		// Đi qua JwtAuthenticationFilter thật (không dùng post-processor authentication()) để chắc rằng lượt dispatch
		// ASYNC lúc SseEmitter hoàn tất không bị Spring Security chặn rồi ghi đè JSON 401 vào cuối luồng SSE.
		JwtDecoder decoder = context.getBean(JwtDecoder.class);
		when(decoder.decode("member-token")).thenReturn(Jwt.withTokenValue("member-token")
				.header("alg", "none").subject("123").claim("type", "access").claim("role", "MEMBER")
				.claim("email", "member@test.local")
				.issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build());
		AssistantChatService chat = context.getBean(AssistantChatService.class);
		when(chat.stream(any(AssistantUser.class), eq("c-1"), eq("Xin chào"))).thenReturn(Flux.just("Chào bạn"));

		MvcResult started = mvc.perform(post("/api/assistant/conversations/c-1/stream")
						.header("Authorization", "Bearer member-token")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"message\":\"Xin chào\"}"))
				.andExpect(request().asyncStarted())
				.andReturn();

		String body = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
						.asyncDispatch(started))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);

		// Chỉ có các khung SSE; không được có ApiResponse lỗi ("code") của entry point/access-denied handler nối vào
		assertThat(body).contains("\"text\":\"Chào bạn\"").contains("event:done").doesNotContain("\"code\"");
	}
	@Test
	void rejectsBlankMessage() throws Exception {
		mvc.perform(post("/api/assistant/conversations/c-1/stream")
						.with(authentication(actor(UserRole.MEMBER)))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"message\":\"  \"}"))
				.andExpect(status().isBadRequest());
	}
}
