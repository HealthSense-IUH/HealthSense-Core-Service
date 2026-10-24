package fit.iuh.se.hsassistant.knowledge;

import org.springframework.ai.vectorstore.qdrant.autoconfigure.QdrantVectorStoreAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Qdrant chỉ được khởi tạo khi thật sự cần (tra cứu hoặc ingest). Autoconfig gốc bị loại ở application.properties
 * (spring.autoconfigure.exclude) vì client gRPC của nó kiểm tra phiên bản server ngay lúc khởi động: môi trường
 * không tới được Qdrant (dev tắt RAG) sẽ chờ timeout hơn một phút mới lên được app.
 */
@Configuration
@ConditionalOnExpression("${app.rag.enabled:true} or ${app.rag.ingest.enabled:false}")
@Import(QdrantVectorStoreAutoConfiguration.class)
public class KnowledgeVectorStoreConfig {
}
