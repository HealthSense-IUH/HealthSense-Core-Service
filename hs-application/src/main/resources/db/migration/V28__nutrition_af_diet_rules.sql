-- =====================================================================
-- V28: Bộ quy tắc chấm màu thực phẩm cho người rung nhĩ (AF)
--
-- Áp cho mọi người (đơn ăn uống của bác sĩ chồng thêm lên), theo thứ tự ưu tiên:
--   1. Lọc cứng: cồn (có cồn là đỏ), caffeine và đường (vượt ngưỡng là vàng/đỏ).
--   2. Tỷ lệ natri/kali: đỏ khi Na/K vượt ngưỡng đỏ VÀ natri vượt ngưỡng đỏ của muối; tốt khi Na/K <= mức tốt.
--   3. Natri, chất béo bão hòa: vượt ngưỡng vàng là vàng, vượt ngưỡng đỏ là đỏ.
--   4. Magie: tốt khi magie >= mức tốt VÀ natri <= ngưỡng vàng của muối.
--
-- Từ V28 so sánh là "vượt quá" (>), không còn "từ mức này trở lên" như V27, cho khớp cách viết của các ngưỡng
-- (ví dụ natri <= 140 tốt, 140-400 vàng, > 400 đỏ). good_threshold: mức tốt cho nhịp tim (Na/K: từ mức này trở
-- xuống; magie: từ mức này trở lên). Ngưỡng do admin sửa ở trang quản trị; evidence là nguồn của từng quy tắc.
--
-- Đường: USDA FNDDS không có đường bổ sung nên dùng đường tổng, và không chấm đường cho trái cây, sữa (đường tự
-- nhiên) - phần này nằm trong code (DietAdvisor). Caffeine và đường là ngưỡng thận trọng của hệ thống: thử nghiệm
-- I-STOP-AFib chỉ thấy cồn làm tăng cơn rung nhĩ.
--
-- Ngưỡng của 3 quy tắc cũ chỉ đổi sang giá trị mới khi admin chưa sửa (còn đúng giá trị khởi tạo của V27).
-- =====================================================================

ALTER TABLE nutrition_diet_rules DROP CONSTRAINT nutrition_diet_rules_code_check;
ALTER TABLE nutrition_diet_rules ADD CONSTRAINT nutrition_diet_rules_code_check CHECK (code IN
    ('ALCOHOL', 'CAFFEINE', 'SUGARS', 'NA_K_RATIO', 'SODIUM', 'SATURATED_FAT', 'MAGNESIUM', 'VITAMIN_K'));

ALTER TABLE nutrition_diet_rules
    ADD COLUMN good_threshold NUMERIC(10, 3) CHECK (good_threshold >= 0),
    ADD COLUMN evidence TEXT,
    ADD COLUMN evidence_url VARCHAR(500);

UPDATE nutrition_diet_rules SET limit_threshold = 400, caution_threshold = 140,
    updated_at = CURRENT_TIMESTAMP, updated_by = 'migration:nutrition-af-rules-v28'
WHERE code = 'SODIUM' AND limit_threshold = 600 AND caution_threshold = 120;

UPDATE nutrition_diet_rules SET limit_threshold = 0,
    updated_at = CURRENT_TIMESTAMP, updated_by = 'migration:nutrition-af-rules-v28'
WHERE code = 'ALCOHOL' AND limit_threshold = 0.5 AND caution_threshold IS NULL;

UPDATE nutrition_diet_rules SET caution_threshold = 80,
    updated_at = CURRENT_TIMESTAMP, updated_by = 'migration:nutrition-af-rules-v28'
WHERE code = 'CAFFEINE' AND limit_threshold IS NULL AND caution_threshold = 10;

INSERT INTO nutrition_diet_rules (code, name, unit, limit_threshold, caution_threshold, good_threshold, display_order, created_at, updated_at, created_by, updated_by) VALUES
    ('SUGARS',        'Đường (đường tổng)',  'g',  10,  2.5, NULL, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'migration:nutrition-af-rules-v28', 'migration:nutrition-af-rules-v28'),
    ('NA_K_RATIO',    'Tỷ lệ natri/kali',    '',   2,   NULL, 1,   4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'migration:nutrition-af-rules-v28', 'migration:nutrition-af-rules-v28'),
    ('SATURATED_FAT', 'Chất béo bão hòa',    'g',  5,   1.5, NULL, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'migration:nutrition-af-rules-v28', 'migration:nutrition-af-rules-v28'),
    ('MAGNESIUM',     'Magie',               'mg', NULL, NULL, 50, 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'migration:nutrition-af-rules-v28', 'migration:nutrition-af-rules-v28');

UPDATE nutrition_diet_rules SET display_order = CASE code
    WHEN 'ALCOHOL' THEN 1 WHEN 'CAFFEINE' THEN 2 WHEN 'SUGARS' THEN 3 WHEN 'NA_K_RATIO' THEN 4
    WHEN 'SODIUM' THEN 5 WHEN 'SATURATED_FAT' THEN 6 WHEN 'MAGNESIUM' THEN 7 WHEN 'VITAMIN_K' THEN 8 END;

UPDATE nutrition_diet_rules SET evidence = CASE code
    WHEN 'ALCOHOL' THEN
        'Marcus GM, et al. Individualized Triggers of Paroxysmal Atrial Fibrillation: The I-STOP-AFib Randomized Clinical Trial. JAMA Cardiology 2022;7(2):167-174. Cồn là yếu tố duy nhất làm tăng rõ số cơn rung nhĩ.'
    WHEN 'CAFFEINE' THEN
        'Ngưỡng thận trọng của hệ thống. Thử nghiệm I-STOP-AFib (Marcus GM, et al. JAMA Cardiology 2022;7(2):167-174) không thấy caffeine làm tăng cơn rung nhĩ.'
    WHEN 'SUGARS' THEN
        'Ngưỡng thận trọng của hệ thống, tính trên đường tổng (USDA FNDDS không có đường bổ sung); không áp cho trái cây và sữa. Tham khảo: Lichtenstein AH, et al. 2021 Dietary Guidance to Improve Cardiovascular Health. Circulation 2021;144:e472-e487.'
    WHEN 'NA_K_RATIO' THEN
        'Kieneker LM, et al. Low potassium excretion but not high sodium excretion is associated with increased risk of developing atrial fibrillation: the PREVEND study. Europace 2014. Bổ trợ: Cook NR, et al. Joint effects of sodium and potassium intake on subsequent cardiovascular disease. Arch Intern Med 2009;169(1):32-40.'
    WHEN 'SODIUM' THEN
        'Fung TT, et al. Adherence to a DASH-Style Diet and Risk of Coronary Heart Disease and Stroke in Women. Arch Intern Med 2008;168(7):713-720. Lichtenstein AH, et al. Circulation 2021;144:e472-e487. Mốc trên 100 g theo quy ước nhãn dinh dưỡng (natri thấp <= 140 mg).'
    WHEN 'SATURATED_FAT' THEN
        'Fung TT, et al. Arch Intern Med 2008;168(7):713-720 (DASH). Lichtenstein AH, et al. Circulation 2021;144:e472-e487. Mốc trên 100 g theo nhãn dinh dưỡng FSA (Anh): thấp <= 1,5 g, cao > 5 g.'
    WHEN 'MAGNESIUM' THEN
        'Khan AM, et al. Low Serum Magnesium and the Development of Atrial Fibrillation in the Community: The Framingham Heart Study. Circulation 2013;127(1):33-38.'
    WHEN 'VITAMIN_K' THEN
        'Warfarin đối kháng vitamin K: người đang dùng warfarin nên giữ lượng vitamin K ổn định mỗi ngày.'
    END,
    evidence_url = CASE code
    WHEN 'ALCOHOL' THEN 'https://jamanetwork.com/journals/jamacardiology/fullarticle/2786196'
    WHEN 'CAFFEINE' THEN 'https://jamanetwork.com/journals/jamacardiology/fullarticle/2786196'
    WHEN 'NA_K_RATIO' THEN 'https://academic.oup.com/europace/article/16/12/1800/489097'
    WHEN 'SODIUM' THEN 'https://pubmed.ncbi.nlm.nih.gov/18413553/'
    WHEN 'SATURATED_FAT' THEN 'https://pubmed.ncbi.nlm.nih.gov/18413553/'
    WHEN 'MAGNESIUM' THEN 'https://www.ahajournals.org/doi/10.1161/CIRCULATIONAHA.111.082511'
    END;
