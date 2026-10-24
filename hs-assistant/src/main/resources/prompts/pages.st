# Danh sách trang của Frontend-Web, mỗi dòng: vai trò được mở | đường dẫn | tên trang | dùng để làm gì
# Vai trò: MEMBER, DOCTOR, ADMIN, SUPER_ADMIN, CARE_COORDINATOR; nhiều vai trò cách nhau bằng dấu phẩy; ALL = mọi vai trò.
# AssistantPageCatalog đọc file này và chỉ đưa vào system prompt những dòng khớp vai trò của người đang chat.
MEMBER | /app/general/dashboard | Bảng điều khiển | tổng quan sức khoẻ hôm nay, lần đo gần nhất, nhắc việc
MEMBER | /app/general/afib-history | Lịch sử đo | danh sách các lần đo PPG, kết quả rung nhĩ, độ tin cậy, tải tệp đo
MEMBER | /app/general/reports | Báo cáo sức khoẻ | báo cáo tổng hợp theo kỳ để xem hoặc mang đi khám
MEMBER | /app/general/statistics/health | Thống kê của tôi: sức khoẻ | biểu đồ số lần đo, xu hướng kết quả theo ngày, tuần, tháng
MEMBER | /app/general/statistics/consultations | Thống kê của tôi: tư vấn | số phiên tư vấn, trạng thái theo thời gian
MEMBER | /app/general/statistics/payments | Thống kê của tôi: thanh toán | lịch sử chi tiêu lượt tư vấn
MEMBER | /app/general/consultations | Tư vấn sức khoẻ | đặt phiên tư vấn với bác sĩ, xem phiên đang diễn ra, lịch sử phiên
MEMBER | /app/general/consultations?tab=credits | Lượt tư vấn | mua lượt tư vấn, xem số lượt còn lại và đơn hàng
MEMBER | /app/general/care-history | Lịch sử chăm sóc | các đợt chăm sóc đã kết thúc và tóm tắt của bác sĩ
MEMBER | /app/general/nutrition | Ăn uống | gợi ý dinh dưỡng, chế độ ăn bác sĩ kê, tra cứu thực phẩm
MEMBER | /app/general/profile | Hồ sơ cá nhân | xem và sửa thông tin cá nhân, định danh CCCD, ảnh đại diện, đổi mật khẩu
DOCTOR | /app/management/doctor/consultations | Phiên khám Bác sĩ | danh sách phiên tư vấn được giao, trò chuyện với bệnh nhân, xem hồ sơ đo
DOCTOR | /app/doctor/statistics | Thống kê khám bệnh | số phiên, bệnh nhân theo thời gian
ADMIN,SUPER_ADMIN | /app/management/statistics | Thống kê hệ thống | người dùng, phiên tư vấn, bản ghi đo, thanh toán toàn hệ thống
ADMIN,SUPER_ADMIN | /app/management/users | Quản lý người dùng | tìm, xem chi tiết, khoá hoặc mở tài khoản
ADMIN,SUPER_ADMIN | /app/management/credit-operations | Quản lý lượt tư vấn | cấp, điều chỉnh lượt tư vấn, xem đơn hàng
ADMIN,SUPER_ADMIN | /app/management/health-records | Hồ sơ sức khoẻ bệnh nhân | tra cứu mọi bản ghi đo trong hệ thống
ADMIN,SUPER_ADMIN | /app/management/nutrition-rules | Ngưỡng đánh giá dinh dưỡng | cấu hình ngưỡng và quy tắc dinh dưỡng
ALL | /app/general/profile | Hồ sơ cá nhân | thông tin tài khoản của chính mình
