package fit.iuh.se.hsassistant.knowledge;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Queue ingest tài liệu tri thức, gắn vào exchange chung của hệ thống (đã khai báo ở hs-health-record).
 * Mọi môi trường dùng chung queue này; chỉ instance bật app.rag.ingest.enabled mới consume.
 */
@Configuration
public class KnowledgeRabbitConfig {

	@Value("${app.rabbitmq.exchange:health.record.exchange}")
	private String exchange;

	@Value("${app.rabbitmq.queue.ingest:document.ingest.queue}")
	private String ingestQueue;

	@Value("${app.rabbitmq.routing-key.ingest:document.ingest.routing}")
	private String ingestRoutingKey;

	@Bean
	public Queue knowledgeIngestQueue() {
		return new Queue(ingestQueue, true);
	}

	@Bean
	public Binding knowledgeIngestBinding() {
		return BindingBuilder.bind(knowledgeIngestQueue())
				.to(new DirectExchange(exchange))
				.with(ingestRoutingKey);
	}
}
