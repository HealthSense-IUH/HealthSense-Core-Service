-- Bổ sung cột af_alert, model_version và index tối ưu truy vấn theo thời gian cho health_records
ALTER TABLE health_records
    ADD COLUMN IF NOT EXISTS af_alert BOOLEAN,
    ADD COLUMN IF NOT EXISTS model_version VARCHAR(50);

CREATE INDEX IF NOT EXISTS idx_health_record_user_created_desc
    ON health_records (user_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_health_record_status_updated
    ON health_records (status, updated_at);
