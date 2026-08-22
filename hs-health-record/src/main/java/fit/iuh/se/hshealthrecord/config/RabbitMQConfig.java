package fit.iuh.se.hshealthrecord.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange:health.record.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.queue.processing:health.record.processing.queue}")
    private String processingQueue;

    @Value("${app.rabbitmq.routing-key.processing:health.record.process.routing}")
    private String processingRoutingKey;

    @Bean
    public DirectExchange healthRecordExchange() {
        return new DirectExchange(exchange);
    }

    @Bean
    public Queue healthRecordProcessingQueue() {
        return new Queue(processingQueue, true);
    }

    @Bean
    public Binding healthRecordProcessingBinding(Queue healthRecordProcessingQueue, DirectExchange healthRecordExchange) {
        return BindingBuilder.bind(healthRecordProcessingQueue)
                .to(healthRecordExchange)
                .with(processingRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
