-- =========================================================================
-- Health Record & Workout Module Database Migration
-- Naming: V20260928220000__create_workout_tables.sql
-- =========================================================================

-- 1. Bảng danh mục bài tập (workout_exercises)
CREATE TABLE IF NOT EXISTS workout_exercises (
    id BIGINT NOT NULL PRIMARY KEY,
    user_id BIGINT,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    tracking_type VARCHAR(50) NOT NULL,
    met_rate DOUBLE PRECISION NOT NULL,
    icon_name VARCHAR(100),
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_workout_exercise_user ON workout_exercises (user_id);
CREATE INDEX IF NOT EXISTS idx_workout_exercise_category ON workout_exercises (category);
CREATE INDEX IF NOT EXISTS idx_workout_exercise_code ON workout_exercises (code);

-- 2. Bảng bài tập yêu thích của người dùng (user_favorite_exercises)
CREATE TABLE IF NOT EXISTS user_favorite_exercises (
    id BIGINT NOT NULL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    exercise_code VARCHAR(100) NOT NULL,
    display_order INTEGER,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_user_exercise_fav UNIQUE (user_id, exercise_code)
);

CREATE INDEX IF NOT EXISTS idx_fav_exercise_user ON user_favorite_exercises (user_id);

-- 3. Bảng kế hoạch bài tập (workout_routines)
CREATE TABLE IF NOT EXISTS workout_routines (
    id BIGINT NOT NULL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    has_warmup BOOLEAN NOT NULL DEFAULT FALSE,
    warmup_duration_sec INTEGER,
    has_cooldown BOOLEAN NOT NULL DEFAULT FALSE,
    cooldown_duration_sec INTEGER,
    items_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_workout_routine_user ON workout_routines (user_id);

-- 4. Bảng lịch sử buổi tập (workout_sessions)
CREATE TABLE IF NOT EXISTS workout_sessions (
    id BIGINT NOT NULL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    exercise_code VARCHAR(100) NOT NULL,
    exercise_name VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    tracking_type VARCHAR(50) NOT NULL,
    icon_name VARCHAR(100),
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ended_at TIMESTAMP WITH TIME ZONE NOT NULL,
    duration_seconds INTEGER NOT NULL,
    calories_burned INTEGER NOT NULL,
    total_calories INTEGER,
    distance_meters DOUBLE PRECISION,
    avg_speed_kmh DOUBLE PRECISION,
    total_steps INTEGER,
    completed_sets INTEGER,
    target_type VARCHAR(30),
    target_value DOUBLE PRECISION,
    gpx_track_json TEXT,
    is_heart_rate_monitored BOOLEAN NOT NULL DEFAULT FALSE,
    avg_heart_rate INTEGER,
    max_heart_rate INTEGER,
    encoded_polyline TEXT,
    gpx_track_url VARCHAR(512),
    telemetry_json_url VARCHAR(512),
    note TEXT,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_workout_session_user ON workout_sessions (user_id);
CREATE INDEX IF NOT EXISTS idx_workout_session_user_date ON workout_sessions (user_id, started_at);

-- 5. Bảng hồ sơ sức khỏe / đo tầm soát Afib (health_records)
CREATE TABLE IF NOT EXISTS health_records (
    id BIGINT NOT NULL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    s3_file_key VARCHAR(500) NOT NULL,
    file_size BIGINT,
    status VARCHAR(30) NOT NULL,
    prediction_label VARCHAR(30),
    confidence DOUBLE PRECISION,
    hrv_features_json TEXT,
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_health_record_user ON health_records (user_id);
CREATE INDEX IF NOT EXISTS idx_health_record_user_status_date ON health_records (user_id, status, created_at);

-- 6. Bảng thống kê sức khỏe hàng ngày (health_statistics_daily)
CREATE TABLE IF NOT EXISTS health_statistics_daily (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    stat_date TIMESTAMP WITH TIME ZONE,
    total_records BIGINT,
    total_normal INTEGER,
    total_afib INTEGER,
    total_uncertain INTEGER,
    total_afib_suspected INTEGER,
    CONSTRAINT uq_user_stat_date UNIQUE (user_id, stat_date)
);

-- 7. Seed data danh mục bài tập hệ thống chuẩn (Chỉ chèn nếu chưa tồn tại)
INSERT INTO workout_exercises (id, user_id, code, name, category, tracking_type, met_rate, icon_name, is_system, description, created_at)
VALUES
    (1001, NULL, 'walking', 'Đi bộ', 'GENERAL', 'DISTANCE_GPS', 3.5, 'Footprints', TRUE, 'Đi bộ tự nhiên giúp tăng cường tuần hoàn máu và thư giãn tinh thần.', NOW()),
    (1002, NULL, 'running', 'Chạy bộ', 'AEROBIC', 'DISTANCE_GPS', 8.0, 'Activity', TRUE, 'Chạy bộ tăng cường sức bền, cải thiện VO2 max và sức khỏe tim mạch.', NOW()),
    (1003, NULL, 'cycling', 'Đạp xe', 'AEROBIC', 'DISTANCE_GPS', 6.0, 'Bike', TRUE, 'Đạp xe ngoài trời hoặc máy đạp xe rèn luyện cơ chân và sức bền.', NOW()),
    (1004, NULL, 'badminton', 'Cầu lông', 'BALL', 'TIME_CALORIES', 5.5, 'Trophy', TRUE, 'Môn thể thao phản xạ nhanh, di chuyển liên tục và rèn luyện toàn thân.', NOW()),
    (1005, NULL, 'swimming', 'Bơi lội', 'WATER', 'TIME_CALORIES', 7.0, 'Waves', TRUE, 'Bơi lội toàn diện phát triển cơ bắp, dung tích phổi và giảm áp lực khớp.', NOW()),
    (1006, NULL, 'combined_workout', 'Bài tập kết hợp', 'GENERAL', 'TIME_CALORIES', 5.0, 'Dumbbell', TRUE, 'Tổ hợp các động tác thể chất rèn luyện sức bền và toàn thân.', NOW()),
    (1007, NULL, 'stretching', 'Giãn cơ', 'GENERAL', 'TIME_CALORIES', 2.5, 'Sparkles', TRUE, 'Kéo giãn cơ bắp, hỗ trợ phục hồi và tăng tính linh hoạt của khớp.', NOW()),
    (1008, NULL, 'yoga', 'Yoga', 'GENERAL', 'TIME_CALORIES', 3.0, 'HeartPulse', TRUE, 'Tập trung hơi thở, kéo giãn và cân bằng năng lượng cơ thể.', NOW()),
    (1009, NULL, 'jump_rope', 'Nhảy dây', 'AEROBIC', 'TIME_CALORIES', 9.0, 'Flame', TRUE, 'Đốt calo cường độ cao, phát triển sức bật và nhịp điệu vận động.', NOW()),
    (1010, NULL, 'strength_training', 'Tập tạ', 'FREE_WEIGHT', 'SETS_REST', 4.5, 'Dumbbell', TRUE, 'Rèn luyện sức mạnh, kích thích phát triển cơ bắp và mật độ xương.', NOW())
ON CONFLICT (id) DO NOTHING;
