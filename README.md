# HealthSense API Service

## Tiếng Việt

**HealthSense API Service** là hệ thống Backend Microservice trung tâm của hệ sinh thái HealthSense, được phát triển bằng Java 21 và Spring Boot 3.x với kiến trúc Maven Multi-Module. Hệ thống đóng vai trò quản lý tài khoản, phiên làm việc, lưu trữ hồ sơ sức khỏe (BPM, SpO2, dữ liệu PPG) và tích hợp các dịch vụ AI/Notification.

### Chức năng chính
- **Xác thực & Phân quyền (Auth):** Hỗ trợ đăng nhập/đăng ký cho cả Web và Mobile App, quản lý JWT token, Refresh Token và phiên làm việc (Session Management).
- **Quản lý Hồ sơ Sức khỏe (Health Record):** Nhận và lưu trữ nhật ký nhịp tim (BPM), nồng độ Oxy trong máu (SpO2), dữ liệu đo từ đồng hồ đeo tay HuyWatch.
- **Tư vấn & Chatbot (Chat):** Xử lý luồng hội thoại tư vấn sức khỏe.
- **Hệ thống Thông báo (Notification):** Gửi thông báo nhắc nhở và cảnh báo chỉ số bất thường.
- **Quản lý Người dùng (User):** Quản lý thông tin cá nhân và cài đặt ứng dụng.

### Công nghệ
- **Ngôn ngữ:** Java 21
- **Framework:** Spring Boot 3.x, Spring Security, Spring Data JPA
- **Build Tool:** Maven (Multi-Module)
- **Database:** PostgreSQL / MySQL
- **Containerization:** Docker & Docker Compose, Jib Maven Plugin

### Cấu trúc dự án
Dự án được chia thành 7 module Maven:
- `hs-application`: Module chính chứa điểm khởi chạy ứng dụng (Entrypoint) và cấu hình tổng thể.
- `hs-auth`: Xử lý xác thực, cấp phát/làm mới JWT, quản lý Session.
- `hs-health-record`: Lưu trữ và xử lý truy vấn dữ liệu sinh lý (BPM, SpO2, PPG).
- `hs-user`: Quản lý hồ sơ người dùng và thông tin cá nhân.
- `hs-chat`: Xử lý chức năng chat tư vấn sức khỏe.
- `hs-notification`: Quản lý và phát thông báo.
- `hs-shared`: Chứa các DTO, Utilities, Constants và Handler xử lý ngoại lệ chung.

### Cài đặt và Sử dụng
1. Cấu hình môi trường cơ sở dữ liệu trong `application.yml` hoặc file môi trường.
2. Biên dịch và đóng gói dự án:
   ```bash
   ./mvnw clean install
   ```
3. Chạy ứng dụng Backend:
   ```bash
   ./mvnw spring-boot:run -pl hs-application
   ```
4. Chạy bằng Docker Compose (tùy chọn):
   ```bash
   docker-compose up -d
   ```

---

## English

**HealthSense API Service** is the central backend engine for the HealthSense health monitoring ecosystem, built with Java 21 and Spring Boot 3.x using a Maven Multi-Module architecture. It manages user authentication, session control, health records (BPM, SpO2, PPG data), and integration with AI/Notification services.

### Key Features
- **Authentication & Security (Auth):** Supports Web and Mobile login/register flows, JWT token issuance, Refresh Token handling, and Session Management.
- **Health Record Management (Health Record):** Receives and stores heart rate (BPM), blood oxygen saturation (SpO2), and PPG measurement data from the HuyWatch device.
- **Health Chatbot & Advice (Chat):** Manages conversational health advice flows.
- **Notification System (Notification):** Delivers push notifications and abnormal health metric alerts.
- **User Profile Management (User):** Manages user profiles and account settings.

### Tech Stack
- **Language:** Java 21
- **Framework:** Spring Boot 3.x, Spring Security, Spring Data JPA
- **Build Tool:** Maven (Multi-Module)
- **Database:** PostgreSQL / MySQL
- **Containerization:** Docker & Docker Compose, Jib Maven Plugin

### Project Structure
Organized into 7 Maven submodules:
- `hs-application`: Main entrypoint module containing Spring Boot startup configuration.
- `hs-auth`: Authentication, JWT lifecycle, and session management logic.
- `hs-health-record`: Physiological record storage and query APIs (BPM, SpO2, PPG).
- `hs-user`: User profile management and account details.
- `hs-chat`: Health consultation chat logic.
- `hs-notification`: Notification delivery service.
- `hs-shared`: Shared DTOs, utilities, constants, and global exception handlers.

### Installation and Usage
1. Configure database connection settings in `application.yml` or environment variables.
2. Build and package the project:
   ```bash
   ./mvnw clean install
   ```
3. Run the backend service:
   ```bash
   ./mvnw spring-boot:run -pl hs-application
   ```
4. Run via Docker Compose (optional):
   ```bash
   docker-compose up -d
   ```
