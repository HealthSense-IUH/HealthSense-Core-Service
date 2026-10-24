-- Kho tri thức cho trợ lý (hs-assistant): tài liệu do admin tải lên, được tách đoạn + embedding (Ollama)
-- và lưu vector ở Qdrant. Bảng này chỉ giữ metadata + trạng thái ingest; nội dung file nằm ở MinIO/S3.
CREATE TABLE IF NOT EXISTS knowledge_documents (
    id            VARCHAR(36)  NOT NULL PRIMARY KEY,
    title         VARCHAR(200) NOT NULL,
    file_name     VARCHAR(255) NOT NULL,
    content_type  VARCHAR(120),
    size_bytes    BIGINT       NOT NULL DEFAULT 0,
    object_key    VARCHAR(512) NOT NULL,
    status        VARCHAR(20)  NOT NULL CHECK (status IN ('PENDING', 'PROCESSING', 'READY', 'FAILED')),
    chunk_count   INT          NOT NULL DEFAULT 0,
    error_message VARCHAR(1000),
    uploaded_by   BIGINT,
    created_at    TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT now(),
    processed_at  TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_knowledge_documents_status ON knowledge_documents (status);
CREATE INDEX IF NOT EXISTS idx_knowledge_documents_created ON knowledge_documents (created_at DESC);
