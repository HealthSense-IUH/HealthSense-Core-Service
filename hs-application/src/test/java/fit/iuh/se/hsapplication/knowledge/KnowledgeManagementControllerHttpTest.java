package fit.iuh.se.hsapplication.knowledge;

import fit.iuh.se.hsapplication.config.security.JwtAuthenticationFilter;
import fit.iuh.se.hsapplication.config.security.RateLimitFilter;
import fit.iuh.se.hsapplication.config.security.RestAccessDeniedHandler;
import fit.iuh.se.hsapplication.config.security.RestAuthenticationEntryPoint;
import fit.iuh.se.hsapplication.config.security.SecurityConfig;
import fit.iuh.se.hsapplication.controller.admin.KnowledgeManagementController;
import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hsapplication.service.ratelimit.RateLimiterService;
import fit.iuh.se.hsassistant.dto.KnowledgeDocumentResponse;
import fit.iuh.se.hsassistant.entity.KnowledgeDocumentStatus;
import fit.iuh.se.hsassistant.knowledge.KnowledgeDocumentService;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Lớp HTTP của kho tri thức: chỉ ADMIN/SUPER_ADMIN vào được /api/admin/knowledge/**, hội viên bị 403;
 * upload multipart và danh sách phân trang đi đúng vào service (được mock).
 */
class KnowledgeManagementControllerHttpTest {

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
	@Import({SecurityConfig.class, KnowledgeManagementController.class, JwtAuthenticationFilter.class,
			RateLimitFilter.class, RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class,
			fit.iuh.se.hsshared.advice.handler.GlobalExceptionHandler.class})
	static class Config {
		@Bean
		KnowledgeDocumentService knowledgeDocumentService() {
			return mock(KnowledgeDocumentService.class);
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
		UserAuthentication principal = new UserAuthentication(7L, "admin@test.local", role);
		return new UsernamePasswordAuthenticationToken(principal, null,
				List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));
	}

	private static KnowledgeDocumentResponse sample() {
		return new KnowledgeDocumentResponse("k-1", "Hướng dẫn sử dụng", "guide.pdf", "application/pdf", 1234,
				KnowledgeDocumentStatus.READY, 12, null, 7L, Instant.now(), Instant.now(), Instant.now());
	}

	@Test
	void rejectsAnonymousAndMembers() throws Exception {
		mvc.perform(get("/api/admin/knowledge/documents")).andExpect(status().isUnauthorized());
		mvc.perform(get("/api/admin/knowledge/documents").with(authentication(actor(UserRole.MEMBER))))
				.andExpect(status().isForbidden());
		mvc.perform(get("/api/admin/knowledge/documents").with(authentication(actor(UserRole.DOCTOR))))
				.andExpect(status().isForbidden());
	}

	@Test
	void listsDocumentsForAdmin() throws Exception {
		KnowledgeDocumentService service = context.getBean(KnowledgeDocumentService.class);
		when(service.list(eq("guide"), any(Pageable.class)))
				.thenReturn(new PageResponse<>(new PageImpl<>(List.of(sample()), PageRequest.of(0, 10), 1)));

		mvc.perform(get("/api/admin/knowledge/documents").param("keyword", "guide")
						.with(authentication(actor(UserRole.ADMIN))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content[0].id").value("k-1"))
				.andExpect(jsonPath("$.data.content[0].status").value("READY"))
				.andExpect(jsonPath("$.data.totalElements").value(1));
	}

	@Test
	void uploadsMultipartForSuperAdmin() throws Exception {
		KnowledgeDocumentService service = context.getBean(KnowledgeDocumentService.class);
		when(service.upload(any(MultipartFile.class), eq("Tài liệu A"), eq(7L))).thenReturn(sample());

		mvc.perform(multipart("/api/admin/knowledge/documents")
						.file(new MockMultipartFile("file", "guide.pdf", "application/pdf", new byte[] {1, 2, 3}))
						.param("title", "Tài liệu A")
						.with(authentication(actor(UserRole.SUPER_ADMIN))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.title").value("Hướng dẫn sử dụng"));
	}

	@Test
	void deletesDocumentForAdmin() throws Exception {
		KnowledgeDocumentService service = context.getBean(KnowledgeDocumentService.class);

		mvc.perform(delete("/api/admin/knowledge/documents/k-1").with(authentication(actor(UserRole.ADMIN))))
				.andExpect(status().isNoContent());

		verify(service).delete("k-1");
		verify(service, org.mockito.Mockito.never()).upload(any(), isNull(), any());
	}
}
