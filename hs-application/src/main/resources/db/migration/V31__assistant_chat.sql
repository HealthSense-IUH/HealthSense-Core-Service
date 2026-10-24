-- Trợ lý AI (hs-assistant)
-- 1) Hội thoại thuộc về người dùng nào: dùng để kiểm tra quyền trước khi đọc/ghi bộ nhớ hội thoại
CREATE TABLE IF NOT EXISTS assistant_conversations (
    id         VARCHAR(36)  NOT NULL PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    title      VARCHAR(160),
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_assistant_conversations_user ON assistant_conversations (user_id, updated_at DESC);

-- 2) Bộ nhớ hội thoại của Spring AI (JdbcChatMemoryRepository), giữ đúng schema-postgresql.sql của Spring AI 2.0
CREATE TABLE IF NOT EXISTS SPRING_AI_CHAT_MEMORY (
    conversation_id VARCHAR(36) NOT NULL,
    content         TEXT        NOT NULL,
    type            VARCHAR(10) NOT NULL CHECK (type IN ('USER', 'ASSISTANT', 'SYSTEM', 'TOOL')),
    "timestamp"     TIMESTAMP   NOT NULL,
    sequence_id     BIGINT      NOT NULL
);

CREATE INDEX IF NOT EXISTS SPRING_AI_CHAT_MEMORY_CONVERSATION_ID_TIMESTAMP_IDX
    ON SPRING_AI_CHAT_MEMORY (conversation_id, "timestamp");

CREATE INDEX IF NOT EXISTS SPRING_AI_CHAT_MEMORY_CONVERSATION_ID_SEQUENCE_ID_IDX
    ON SPRING_AI_CHAT_MEMORY (conversation_id, sequence_id);
