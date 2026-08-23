# User Module Database Migration

## Tiếng Việt

- **Quy tắc đặt tên:** `V<YYYYMMDDHHMMSS>__<mo_ta_ngan_gon>.sql` (Ví dụ: `V20260823190000__create_user_tables.sql`).
- **Cấu hình:** Dự án đã bật `spring.flyway.out-of-order=true`.
- **Lưu ý:** Không sửa file migration đã chạy. Hãy tạo file mới khi cần cập nhật schema.

---

## English

- **Naming Convention:** `V<YYYYMMDDHHMMSS>__<short_description>.sql` (e.g. `V20260823190000__create_user_tables.sql`).
- **Configuration:** `spring.flyway.out-of-order=true` is enabled.
- **Notes:** Never modify executed migration files. Create a new migration file for schema changes.
