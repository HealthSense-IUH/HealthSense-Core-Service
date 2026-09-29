-- =====================================================================
-- V27: Ngưỡng chấm màu thực phẩm không còn cố định trong code
--
-- nutrition_diet_rules: ngưỡng mặc định của cả hệ thống, admin sửa ở trang quản trị.
--   So sánh "từ mức này trở lên", số liệu trên 100 g phần ăn được:
--   giá trị >= limit_threshold -> đỏ (Nên hạn chế); >= caution_threshold -> vàng (Cần lưu ý).
--   Để trống một ngưỡng = quy tắc không có mức đó.
-- nutrition_diet_prescriptions.*_limit / *_caution: bác sĩ chỉnh riêng cho từng hội viên;
--   NULL = dùng ngưỡng mặc định.
-- Giá trị khởi tạo giữ đúng các ngưỡng đang dùng (muối theo nhãn thực phẩm FSA của Anh).
-- =====================================================================

CREATE TABLE nutrition_diet_rules (
    code VARCHAR(30) PRIMARY KEY CHECK (code IN ('SODIUM', 'ALCOHOL', 'CAFFEINE', 'VITAMIN_K')),
    name VARCHAR(120) NOT NULL,
    unit VARCHAR(10) NOT NULL,
    limit_threshold NUMERIC(10, 3) CHECK (limit_threshold >= 0),
    caution_threshold NUMERIC(10, 3) CHECK (caution_threshold >= 0),
    display_order INTEGER NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ,
    created_by VARCHAR(255), updated_by VARCHAR(255),
    CHECK (limit_threshold IS NULL OR caution_threshold IS NULL OR limit_threshold >= caution_threshold)
);

INSERT INTO nutrition_diet_rules (code, name, unit, limit_threshold, caution_threshold, display_order, created_at, updated_at, created_by, updated_by) VALUES
    ('SODIUM',    'Muối (natri)', 'mg', 600, 120,  1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'migration:nutrition-diet-rules-v27', 'migration:nutrition-diet-rules-v27'),
    ('ALCOHOL',   'Cồn',          'g',  0.5, NULL, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'migration:nutrition-diet-rules-v27', 'migration:nutrition-diet-rules-v27'),
    ('CAFFEINE',  'Caffeine',     'mg', NULL, 10,  3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'migration:nutrition-diet-rules-v27', 'migration:nutrition-diet-rules-v27'),
    ('VITAMIN_K', 'Vitamin K',    'µg', NULL, 100, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'migration:nutrition-diet-rules-v27', 'migration:nutrition-diet-rules-v27');

ALTER TABLE nutrition_diet_prescriptions
    ADD COLUMN sodium_limit NUMERIC(10, 3) CHECK (sodium_limit >= 0),
    ADD COLUMN sodium_caution NUMERIC(10, 3) CHECK (sodium_caution >= 0),
    ADD COLUMN alcohol_limit NUMERIC(10, 3) CHECK (alcohol_limit >= 0),
    ADD COLUMN alcohol_caution NUMERIC(10, 3) CHECK (alcohol_caution >= 0),
    ADD COLUMN caffeine_limit NUMERIC(10, 3) CHECK (caffeine_limit >= 0),
    ADD COLUMN caffeine_caution NUMERIC(10, 3) CHECK (caffeine_caution >= 0),
    ADD COLUMN vitamin_k_limit NUMERIC(10, 3) CHECK (vitamin_k_limit >= 0),
    ADD COLUMN vitamin_k_caution NUMERIC(10, 3) CHECK (vitamin_k_caution >= 0);
