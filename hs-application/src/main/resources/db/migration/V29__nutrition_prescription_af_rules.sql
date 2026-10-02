-- =====================================================================
-- V29: Bác sĩ dặn riêng và chỉnh ngưỡng riêng cho cả 4 quy tắc thêm ở V28
--
-- Mỗi cờ ứng với một quy tắc trong nutrition_diet_rules. Quy tắc nền vẫn luôn áp dụng cho mọi người;
-- cờ để lời nhắn ghi "Bác sĩ dặn bạn ..." và để bác sĩ đặt ngưỡng riêng:
--   limit_sugars            -> SUGARS         (sugars_limit, sugars_caution)
--   watch_sodium_potassium  -> NA_K_RATIO     (na_k_ratio_limit, na_k_ratio_good)
--   limit_saturated_fat     -> SATURATED_FAT  (saturated_fat_limit, saturated_fat_caution)
--   encourage_magnesium     -> MAGNESIUM      (magnesium_good)
-- NULL = dùng ngưỡng mặc định của admin. So "vượt quá" trên 100 g; mức tốt: Na/K từ mức này trở xuống,
-- magie từ mức này trở lên.
-- =====================================================================

ALTER TABLE nutrition_diet_prescriptions
    ADD COLUMN limit_sugars BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN watch_sodium_potassium BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN limit_saturated_fat BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN encourage_magnesium BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN sugars_limit NUMERIC(10, 3) CHECK (sugars_limit >= 0),
    ADD COLUMN sugars_caution NUMERIC(10, 3) CHECK (sugars_caution >= 0),
    ADD COLUMN saturated_fat_limit NUMERIC(10, 3) CHECK (saturated_fat_limit >= 0),
    ADD COLUMN saturated_fat_caution NUMERIC(10, 3) CHECK (saturated_fat_caution >= 0),
    ADD COLUMN na_k_ratio_limit NUMERIC(10, 3) CHECK (na_k_ratio_limit >= 0),
    ADD COLUMN na_k_ratio_good NUMERIC(10, 3) CHECK (na_k_ratio_good >= 0),
    ADD COLUMN magnesium_good NUMERIC(10, 3) CHECK (magnesium_good >= 0);
