package fit.iuh.se.hsassistant.knowledge;

import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Ollama không có xác thực, nên khi mở ra ngoài (cho máy dev gọi) nó được đặt sau reverse proxy có Basic Auth.
 * Khi app.ollama.basic-auth=user:pass được đặt, client Ollama gửi kèm header Authorization; để trống thì dùng
 * client mặc định của Spring AI (trên cloud gọi nội bộ http://ollama:11434).
 */
@Configuration
public class OllamaClientConfig {

	@Bean
	@ConditionalOnExpression("!'${app.ollama.basic-auth:}'.isBlank()")
	public OllamaApi ollamaApi(@Value("${spring.ai.ollama.base-url}") String baseUrl,
							   @Value("${app.ollama.basic-auth}") String basicAuth) {
		String header = "Basic " + Base64.getEncoder().encodeToString(basicAuth.trim().getBytes(StandardCharsets.UTF_8));
		return OllamaApi.builder()
				.baseUrl(baseUrl)
				.restClientBuilder(RestClient.builder().defaultHeader(HttpHeaders.AUTHORIZATION, header))
				.webClientBuilder(WebClient.builder().defaultHeader(HttpHeaders.AUTHORIZATION, header))
				.build();
	}
}
