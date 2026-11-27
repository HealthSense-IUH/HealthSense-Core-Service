-- =========================================================================
-- Đồng bộ danh mục 18 bài tập hệ thống cốt lõi (khớp 100% với Mobile)
-- Chỉ cập nhật các bài hệ thống (is_system = TRUE), không ảnh hưởng bài tập
-- tùy chỉnh của người dùng hay lịch sử workout_sessions cũ.
-- =========================================================================

-- 0. Nới rộng CHECK constraint của Hibernate/Postgres để chấp nhận INDOOR_STEP
ALTER TABLE workout_exercises
    DROP CONSTRAINT IF EXISTS workout_exercises_tracking_type_check;
ALTER TABLE workout_exercises
    ADD CONSTRAINT workout_exercises_tracking_type_check
        CHECK (tracking_type IN ('TIME_CALORIES', 'DISTANCE_GPS', 'INDOOR_STEP', 'SETS_REST'));

ALTER TABLE workout_sessions
    DROP CONSTRAINT IF EXISTS workout_sessions_tracking_type_check;
ALTER TABLE workout_sessions
    ADD CONSTRAINT workout_sessions_tracking_type_check
        CHECK (tracking_type IN ('TIME_CALORIES', 'DISTANCE_GPS', 'INDOOR_STEP', 'SETS_REST'));

-- 1. Xóa 3 bài hệ thống cũ không còn dùng trong danh mục tinh gọn 18 bài
DELETE FROM workout_exercises
WHERE is_system = TRUE
  AND code IN ('combined_workout', 'swimming', 'strength_training');

-- 2. Đồng bộ chuẩn xác 18 bài tập hệ thống (ID 1001..1018)
INSERT INTO workout_exercises (
    id, user_id, code, name, category, tracking_type, met_rate, icon_name, is_system, description, created_at, updated_at
)
VALUES
    -- 1. Chung (GENERAL) — 4 bài
    (1001, NULL, 'walking', 'Đi bộ', 'GENERAL', 'DISTANCE_GPS', 3.5, 'Footprints', TRUE,
     'Đi bộ ngoài trời nhẹ nhàng, tốt cho tuần hoàn và sức khỏe tim mạch.', NOW(), NOW()),
    (1011, NULL, 'indoor_walking', 'Đi bộ trong nhà', 'GENERAL', 'INDOOR_STEP', 3.3, 'Footprints', TRUE,
     'Đi bộ trong nhà hoặc trên máy không cần GPS, đo bước chân và quãng đường.', NOW(), NOW()),
    (1008, NULL, 'yoga', 'Yoga', 'GENERAL', 'TIME_CALORIES', 3.0, 'HeartPulse', TRUE,
     'Điều hòa nhịp thở, kéo giãn cơ thể và giảm căng thẳng thần kinh.', NOW(), NOW()),
    (1007, NULL, 'stretching', 'Giãn cơ', 'GENERAL', 'TIME_CALORIES', 2.5, 'Sparkles', TRUE,
     'Kéo giãn cơ bắp, hỗ trợ phục hồi và tăng độ linh hoạt khớp.', NOW(), NOW()),

    -- 2. Hiếu khí / Cardio (AEROBIC) — 5 bài
    (1002, NULL, 'running', 'Chạy bộ', 'AEROBIC', 'DISTANCE_GPS', 7.5, 'Flame', TRUE,
     'Chạy bộ ngoài trời rèn luyện sức bền tim phổi và tiêu hao năng lượng.', NOW(), NOW()),
    (1003, NULL, 'cycling', 'Đạp xe', 'AEROBIC', 'DISTANCE_GPS', 6.0, 'Bike', TRUE,
     'Đạp xe ngoài trời tăng sức bền chân và tuần hoàn máu, ít tác động khớp gối.', NOW(), NOW()),
    (1012, NULL, 'treadmill', 'Máy chạy bộ', 'AEROBIC', 'INDOOR_STEP', 7.0, 'Gauge', TRUE,
     'Chạy bộ trên máy trong nhà, chủ động kiểm soát tốc độ và nhịp tim.', NOW(), NOW()),
    (1013, NULL, 'indoor_cycling', 'Đạp xe trong nhà', 'AEROBIC', 'TIME_CALORIES', 6.8, 'Bike', TRUE,
     'Đạp xe tại chỗ trên máy tập trong nhà, kiểm soát nhịp tim an toàn.', NOW(), NOW()),
    (1009, NULL, 'jump_rope', 'Nhảy dây', 'AEROBIC', 'TIME_CALORIES', 8.8, 'Zap', TRUE,
     'Cardio cường độ cao giúp tăng nhịp điệu vận động và đốt calo nhanh.', NOW(), NOW()),

    -- 3. Sức mạnh / Tập tạ (FREE_WEIGHT — SETS_REST) — 5 bài
    (1014, NULL, 'push_up', 'Hít đất', 'FREE_WEIGHT', 'SETS_REST', 4.0, 'Shield', TRUE,
     'Tăng sức mạnh cơ ngực, vai và tay sau bằng trọng lượng cơ thể.', NOW(), NOW()),
    (1015, NULL, 'squats', 'Squats', 'FREE_WEIGHT', 'SETS_REST', 5.0, 'ArrowDownCircle', TRUE,
     'Tăng sức mạnh cơ đùi, cơ mông và giữ thăng bằng thân dưới.', NOW(), NOW()),
    (1016, NULL, 'plank', 'Plank', 'FREE_WEIGHT', 'SETS_REST', 3.8, 'Activity', TRUE,
     'Giữ tư thế tĩnh giúp khỏe cơ lõi, bụng và ổn định cột sống.', NOW(), NOW()),
    (1017, NULL, 'crunches', 'Gập bụng', 'FREE_WEIGHT', 'SETS_REST', 3.8, 'Disc', TRUE,
     'Tác động trực tiếp lên nhóm cơ bụng trên và cơ lõi trung tâm.', NOW(), NOW()),
    (1018, NULL, 'dumbbell_curl', 'Tập tạ tay', 'FREE_WEIGHT', 'SETS_REST', 4.0, 'Dumbbell', TRUE,
     'Rèn luyện cơ tay và phần thân trên với tạ đơn vừa sức.', NOW(), NOW()),

    -- 4. Nước (WATER) — 1 bài
    (1005, NULL, 'pool_swimming', 'Bơi lội', 'WATER', 'TIME_CALORIES', 6.0, 'Waves', TRUE,
     'Bơi lội toàn thân giúp tăng dung tích phổi và bảo vệ khớp tối đa.', NOW(), NOW()),

    -- 5. Thể thao bóng (BALL) — 3 bài
    (1004, NULL, 'badminton', 'Cầu lông', 'BALL', 'TIME_CALORIES', 5.5, 'Trophy', TRUE,
     'Môn thể thao vợt phổ biến giúp tăng phản xạ và sự linh hoạt.', NOW(), NOW()),
    (1006, NULL, 'soccer', 'Bóng đá', 'BALL', 'DISTANCE_GPS', 7.5, 'Goal', TRUE,
     'Vận động cường độ cao giúp tăng sức bền tim mạch và tinh thần đồng đội.', NOW(), NOW()),
    (1010, NULL, 'pickleball', 'Pickleball', 'BALL', 'TIME_CALORIES', 5.5, 'Target', TRUE,
     'Môn thể thao vợt vừa sức, phù hợp nhiều lứa tuổi và tốt cho tim mạch.', NOW(), NOW())
ON CONFLICT (id) DO UPDATE SET
    code = EXCLUDED.code,
    name = EXCLUDED.name,
    category = EXCLUDED.category,
    tracking_type = EXCLUDED.tracking_type,
    met_rate = EXCLUDED.met_rate,
    icon_name = EXCLUDED.icon_name,
    is_system = TRUE,
    description = EXCLUDED.description,
    updated_at = NOW();
