# HealthSense Database Migration (Flyway)

## Tiếng Việt

Tài liệu quy chuẩn quản lý và viết script Database Migration bằng Flyway trong kiến trúc Maven Multi-Module của dự án HealthSense.

### Quy tắc đặt tên file
Sử dụng định dạng Timestamp (14 chữ số) để tránh trùng lặp version khi nhiều nhánh phát triển song song:

```text
V<YYYYMMDDHHMMSS>__<mo_ta_ngan_gon>.sql
```

- `V`: Tiền tố bắt buộc (chữ in hoa).
- `<YYYYMMDDHHMMSS>`: Thời gian tạo file gồm NămThángNgàyGiờPhútGiây (ví dụ: `20260823190000`).
- `__`: Hai dấu gạch dưới liên tiếp.
- `<mo_ta_ngan_gon>`: Mô tả ngắn gọn nội dung thay đổi bằng tiếng Anh (snake_case).

**Ví dụ:**
- `V20260823190000__create_user_table.sql`
- `V20260823193000__add_avatar_to_user_profiles.sql`

### Vị trí lưu trữ theo module
Mỗi module tự quản lý file migration của mình trong thư mục `src/main/resources/db/migration/<module_name>/`:
- `hs-user`: `hs-user/src/main/resources/db/migration/user/`
- `hs-health-record`: `hs-health-record/src/main/resources/db/migration/health_record/`
- `hs-consultation`: `hs-consultation/src/main/resources/db/migration/consultation/`

### Cấu hình Out-of-Order
Hệ thống đã kích hoạt cấu hình `spring.flyway.out-of-order=true`. Khi merge các nhánh có timestamp cũ hơn vào sau, Flyway vẫn tự động thực thi các file migration này mà không gây lỗi thứ tự.

### Lưu ý quan trọng
- Không chỉnh sửa file migration đã được thực thi trên môi trường chung. Nếu cần thay đổi cấu trúc bảng, hãy tạo file migration mới.
- Ưu tiên sử dụng `CREATE TABLE IF NOT EXISTS` và `CREATE INDEX IF NOT EXISTS`.

---

## English

Guidelines and standards for managing Database Migration scripts with Flyway in the HealthSense Maven Multi-Module architecture.

### File Naming Convention
Use a 14-digit Timestamp format to avoid version conflicts across parallel feature branches:

```text
V<YYYYMMDDHHMMSS>__<short_description>.sql
```

- `V`: Mandatory prefix (uppercase).
- `<YYYYMMDDHHMMSS>`: File creation time formatted as YearMonthDayHourMinuteSecond (e.g., `20260823190000`).
- `__`: Two consecutive underscores.
- `<short_description>`: Brief description in English using `snake_case`.

**Examples:**
- `V20260823190000__create_user_table.sql`
- `V20260823193000__add_avatar_to_user_profiles.sql`

### File Locations by Module
Each module manages its own migration scripts within `src/main/resources/db/migration/<module_name>/`:
- `hs-user`: `hs-user/src/main/resources/db/migration/user/`
- `hs-health-record`: `hs-health-record/src/main/resources/db/migration/health_record/`
- `hs-consultation`: `hs-consultation/src/main/resources/db/migration/consultation/`

### Out-of-Order Configuration
The configuration `spring.flyway.out-of-order=true` is enabled. When merging branches with older timestamps later, Flyway will execute pending migrations without throwing ordering errors.

### Important Notes
- Never modify an already executed migration file. Create a new migration file for schema changes.
- Prefer using `CREATE TABLE IF NOT EXISTS` and `CREATE INDEX IF NOT EXISTS`.
