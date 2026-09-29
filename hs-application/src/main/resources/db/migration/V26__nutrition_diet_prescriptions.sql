-- =====================================================================
-- V26: Đơn ăn uống bác sĩ kê cho hội viên
--
-- Bác sĩ tick vài cờ trong buổi tư vấn; mọi thực phẩm hội viên tra cứu được chấm xanh/vàng/đỏ theo
-- các cờ này (DietAdvisor). Mỗi hội viên một dòng, lần kê sau ghi đè lần trước.
-- member_id, prescribed_by là id người dùng; consultation_session_id là phiên tư vấn khi kê. Không đặt khóa
-- ngoại sang bảng của module khác, giống các bảng nutrition_* trước.
-- =====================================================================

CREATE TABLE nutrition_diet_prescriptions (
    member_id BIGINT PRIMARY KEY,
    limit_sodium BOOLEAN NOT NULL,
    on_warfarin BOOLEAN NOT NULL,
    avoid_alcohol BOOLEAN NOT NULL,
    limit_caffeine BOOLEAN NOT NULL,
    note VARCHAR(1000),
    prescribed_by BIGINT NOT NULL,
    consultation_session_id BIGINT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ, updated_at TIMESTAMPTZ,
    created_by VARCHAR(255), updated_by VARCHAR(255)
);

COMMENT ON COLUMN nutrition_diet_prescriptions.on_warfarin IS 'Dang dung warfarin: nhac giu luong vitamin K on dinh';

-- ---------------------------------------------------------------------
-- Số liệu cồn cho đồ uống của Bảng thành phần thực phẩm Việt Nam (V23): sách ghi độ cồn ngay trong tên
-- ("Bia (cồn: 4,5 g)") chứ không ở cột số liệu, nên V23 để trống. Điền lại đúng con số của sách, g/100 g.
-- Đồ uống không cồn theo bản chất (nước ép, nước khoáng, nước ngọt) ghi 0 để quy tắc "tránh rượu bia"
-- không phải báo "chưa có số liệu".
-- ---------------------------------------------------------------------
UPDATE nutrition_foods f
SET alcohol_g = v.alcohol_g, updated_at = CURRENT_TIMESTAMP, updated_by = 'migration:nutrition-diet-v26'
FROM (VALUES
    ('14001', 4.5),   -- Bia (cồn: 4,5 g)
    ('14002', 32.0),  -- Cô nhắc (cồn 32 g)
    ('14003', 13.0),  -- Cốc tai (cồn 13 g)
    ('14010', 24.2),  -- Rượu cam, chanh (cồn 24,2 g)
    ('14011', 5.0),   -- Rượu nếp (cồn 5 g)
    ('14012', 39.0),  -- Rượu trắng (cồn 39 g)
    ('14013', 9.5),   -- Rượu vang đỏ (cồn 9,5 g)
    ('14014', 9.5),   -- Rượu vang trắng (cồn 9,5 g)
    ('14015', 10.2),  -- Rượu vang trắng ngọt (cồn 10.2 g)
    ('14016', 35.2),  -- Rượu Whisky (cồn 35,2 g)
    ('11013', 0.0),   -- Nước dứa hộp
    ('14004', 0.0),   -- Coca cola
    ('14005', 0.0),   -- Nước cam tươi
    ('14006', 0.0),   -- Nước dừa non tươi
    ('14007', 0.0),   -- Nước ép cà chua
    ('14008', 0.0),   -- Nước khoáng
    ('14009', 0.0)    -- Nước quít tươi
) AS v (source_food_code, alcohol_g)
WHERE f.source = 'VN_FCT' AND f.source_food_code = v.source_food_code AND f.alcohol_g IS NULL;
