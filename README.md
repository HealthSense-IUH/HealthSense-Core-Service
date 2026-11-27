# HealthSense Core Service (`HealthSense-Api-Service`)

## Tiếng Việt

**HealthSense Core Service** là hệ thống Backend trung tâm của nền tảng theo dõi sức khỏe tim mạch và tầm soát rung nhĩ (AFib) HealthSense, được xây dựng bằng **Java 21**, **Spring Boot 4.1.0** và **Spring AI 2.0.1** theo kiến trúc **Modular Monolith (11 Maven Modules)**. Hệ thống đảm nhiệm xác thực/phân quyền, quản lý hồ sơ sức khỏe PPG/HRV, điều phối tư vấn bác sĩ trực tuyến theo hàng đợi FIFO/Round-robin, ví lượt tư vấn bất biến (Immutable Ledger) tích hợp PayOS, cơ sở dữ liệu dinh dưỡng tim mạch (629 thực phẩm), trợ lý AI Cara và API ước tính calo bữa ăn bằng AI.

### Chức năng chính
- **Xác thực & Phân quyền (`hs-auth`, `hs-user`):** Đăng nhập/đăng ký cho Web & Mobile, JWT + Refresh Token rotation, quản lý phiên làm việc, phân quyền RBAC (`SUPER_ADMIN`, `ADMIN`, `DOCTOR`, `MEMBER`).
- **Hồ sơ sức khỏe & Tầm soát AFib (`hs-health-record`):** Cấp Presigned URL tải file PPG `.csv` lên MinIO/S3, gửi sự kiện xử lý bất đồng bộ qua RabbitMQ tới `HealthSense-AI-AFib-Prediction-Service`, tiếp nhận kết quả phân tích V6C Stacking & Global Rule V2 qua Webhook `PATCH /api/health-records/ai-callback` có xác thực chữ ký **HMAC-SHA256** (`X-AI-Timestamp`, `X-AI-Signature`), quản lý trạng thái idempotent và tác vụ phục hồi bản ghi quá hạn (`StuckHealthRecordRecoveryJob`).
- **Theo dõi tập luyện & Bước chân (`hs-health-record`):** Danh mục 18 bài tập cốt lõi đồng bộ Mobile, lịch sử phiên tập, xuất lộ trình GPX thực từ tọa độ GPS và thống kê bước chân hằng ngày.
- **Tư vấn bác sĩ thời gian thực (`hs-consultation`):** Hàng đợi yêu cầu tư vấn FIFO, điều phối bác sĩ khả dụng theo Round-robin (trạng thái giữ chỗ trên Redis), nhắn tin thời gian thực qua WebSocket/STOMP lưu trữ trên MongoDB, cấp quyền chia sẻ hồ sơ sức khỏe theo phiên và lập bản tổng kết y khoa.
- **Ví lượt tư vấn & Thanh toán (`hs-billing`):** Mô hình ví lượt tư vấn, sổ cái giao dịch bất biến (Immutable Ledger), giữ/sử dụng/hoàn lượt nhất quán và tích hợp cổng thanh toán **PayOS** kèm Webhook idempotent.
- **Dinh dưỡng tim mạch & Trợ lý AI (`hs-nutrition`, `hs-assistant`):** Cơ sở dữ liệu 629 thực phẩm sạch (526 Việt Nam + 103 USDA) có chấm điểm theo đơn ăn uống cá nhân do bác sĩ kê; trợ lý AI **Cara** streaming SSE với Tool Calling theo quyền; endpoint `POST /api/nutrition/estimate-meal` (Spring AI Vision/Text) ước tính calo, natri, kali và cảnh báo tim mạch cho bữa ăn.
- **Thông báo & Vận hành (`hs-notification`, `hs-operations`):** Quản lý thông báo đa kênh, nhật ký kiểm toán nghiệp vụ và thống kê hệ thống.

### Công nghệ
- **Ngôn ngữ & Runtime:** Java 21
- **Framework:** Spring Boot 4.1.0, Spring Security, Spring Data JPA, Spring Data MongoDB, Spring Data Redis, Spring AMQP, Spring WebSocket/STOMP, Spring AI 2.0.1 (OpenAI)
- **Migration & Build:** Flyway, Maven Multi-Module, Jib Maven Plugin
- **Cơ sở dữ liệu & Hạ tầng:** PostgreSQL, MongoDB, Redis, RabbitMQ, MinIO (tương thích AWS S3)
- **Triển khai:** Docker, Docker Compose, Dokploy, GitHub Actions CI/CD

### Cấu trúc 11 module Maven
- `hs-application`: Điểm khởi chạy Spring Boot, REST Controllers, SecurityConfig, RateLimitFilter, Schedulers.
- `hs-shared`: DTO chung (`ApiResponse`, `PageResponse`), `AppException`/`GlobalExceptionHandler`, `S3Service`, i18n.
- `hs-auth`: Xác thực JWT, Refresh Token rotation, OTP quên mật khẩu, quản lý phiên.
- `hs-user`: Tài khoản, hồ sơ người dùng, quản trị tài khoản.
- `hs-health-record`: Hồ sơ PPG/HRV, thống kê AFib, danh mục bài tập, phiên tập luyện & bước chân.
- `hs-consultation`: Hàng đợi tư vấn, điều phối bác sĩ, phiên tư vấn, tin nhắn MongoDB, ủy quyền hồ sơ và tổng kết y khoa.
- `hs-billing`: Gói lượt tư vấn, ví lượt, Immutable Ledger, đơn hàng & thanh toán PayOS.
- `hs-nutrition`: Nhóm thực phẩm, 629 thực phẩm tham chiếu (VN_FCT + USDA), khuyến nghị tim mạch, đơn ăn uống và quy tắc chấm điểm.
- `hs-assistant`: Trợ lý AI Cara (SSE streaming + Tool Calling theo quyền) và `MealEstimateService` (ước tính calo/natri từ ảnh hoặc mô tả bữa ăn).
- `hs-notification`: Gửi thông báo realtime và email giao dịch.
- `hs-operations`: Audit logs, hàng đợi tác vụ cần xử lý và thông báo người dùng.

### Cài đặt và Chạy kiểm thử
```bash
# Biên dịch và chạy toàn bộ bộ unit/HTTP test (12 module)
./mvnw -B -T 1C test "-Dtest=!*IntegrationTest,!HsApplicationTests,!*PostgresTest"

# Chạy ứng dụng cục bộ
./mvnw spring-boot:run -pl hs-application
```

---

## English

**HealthSense Core Service** is the central backend platform for the HealthSense cardiovascular monitoring and atrial fibrillation (AFib) screening ecosystem, built with **Java 21**, **Spring Boot 4.1.0**, and **Spring AI 2.0.1** using a **Modular Monolith (11 Maven Modules)** architecture.

### Key Capabilities
- **Authentication & RBAC (`hs-auth`, `hs-user`):** Web & Mobile login/registration, JWT + refresh token rotation, and role-based access (`SUPER_ADMIN`, `ADMIN`, `DOCTOR`, `MEMBER`).
- **Health Records & AFib Screening (`hs-health-record`):** S3/MinIO presigned CSV uploads, asynchronous RabbitMQ dispatch to `HealthSense-AI-AFib-Prediction-Service`, HMAC-SHA256 verified callbacks (`PATCH /api/health-records/ai-callback`), idempotent state guards, and stuck-record recovery.
- **Workout & Steps (`hs-health-record`):** 18 synchronized core exercises, workout history, real GPS-backed GPX export, and daily step tracking.
- **Real-Time Teleconsultation (`hs-consultation`):** FIFO queue, Round-robin doctor dispatch backed by Redis, WebSocket/STOMP messaging persisted in MongoDB, scoped health-record sharing, and finalized medical summaries.
- **Consultation Credits & Billing (`hs-billing`):** Credit wallets, immutable ledger entries, reservation/capture/refund lifecycle, and PayOS webhook integration.
- **Cardiovascular Nutrition & AI Assistant (`hs-nutrition`, `hs-assistant`):** 629 curated reference foods (VN_FCT + USDA) with personalized doctor diet-prescription scoring, **Cara** SSE streaming assistant with role-scoped Tool Calling, and `POST /api/nutrition/estimate-meal` multimodal calorie/sodium estimation.
- **Notifications & Operations (`hs-notification`, `hs-operations`):** Real-time notifications, audit trails, and operational work queues.

### Tech Stack
- **Language:** Java 21
- **Frameworks:** Spring Boot 4.1.0, Spring Security, Spring AI 2.0.1, Flyway, Maven Multi-Module
- **Data & Messaging:** PostgreSQL, MongoDB, Redis, RabbitMQ, MinIO/S3
- **Deployment:** Docker, Docker Compose, Dokploy, GitHub Actions CI/CD
