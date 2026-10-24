package fit.iuh.se.hsassistant.service;

import fit.iuh.se.hsassistant.dto.AssistantUser;
import fit.iuh.se.hschat.service.carehistory.CareHistoryService;
import fit.iuh.se.hschat.service.session.ConsultationSessionService;
import fit.iuh.se.hshealthrecord.service.user.HealthRecordService;
import fit.iuh.se.hshealthrecord.service.workout.WorkoutService;
import fit.iuh.se.hsnutrition.service.DietPrescriptionService;
import fit.iuh.se.hsuser.dto.response.UserResponse;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsuser.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Bộ tool mà mô hình được gọi trong lúc chat. Mỗi tool chỉ đọc dữ liệu của đúng người đang đăng nhập
 * (userId gắn lúc tạo đối tượng), nên mô hình không thể xin dữ liệu của người khác.
 * Tool nào chỉ dành cho hội viên thì trả lời "không áp dụng" với vai trò khác thay vì ném lỗi.
 */
@Slf4j
@RequiredArgsConstructor
public class MemberAssistantTools {

	private final AssistantUser user;
	private final UserService userService;
	private final HealthRecordService healthRecordService;
	private final WorkoutService workoutService;
	private final ConsultationSessionService consultationSessionService;
	private final CareHistoryService careHistoryService;
	private final DietPrescriptionService dietPrescriptionService;

	@Tool(description = "Hồ sơ cá nhân của người đang trò chuyện: tên hiển thị, email, vai trò, giới tính, ngày sinh, số điện thoại, địa chỉ, múi giờ.")
	public Map<String, Object> getMyProfile() {
		UserResponse p = userService.getProfile(user.userId());
		Map<String, Object> out = new LinkedHashMap<>();
		out.put("displayName", p.getDisplayName());
		out.put("email", p.getEmail());
		out.put("role", p.getRole());
		out.put("gender", p.getGender());
		out.put("dateOfBirth", p.getDateOfBirth());
		out.put("phone", p.getPhone());
		out.put("address", p.getAddress());
		out.put("timezone", p.getTimezone());
		out.put("accountStatus", p.getStatus());
		return out;
	}

	@Tool(description = "Lần đo PPG gần nhất của người dùng: trạng thái xử lý, nhãn dự đoán rung nhĩ, độ tin cậy (0-1), thời điểm đo. Trả về null nếu chưa đo lần nào.")
	public Object getLatestHealthRecord() {
		if (!isMember()) return notForRole();
		return healthRecordService.getLatestRecord(user.userId()).orElse(null);
	}

	@Tool(description = "Các lần đo PPG gần đây của người dùng, mới nhất trước. limit từ 1 đến 20.")
	public Object getRecentHealthRecords(@ToolParam(description = "số lần đo muốn lấy, 1-20", required = false) Integer limit) {
		if (!isMember()) return notForRole();
		int n = limit == null ? 5 : Math.max(1, Math.min(20, limit));
		return healthRecordService.getMyRecords(user.userId(),
				PageRequest.of(0, n, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();
	}

	@Tool(description = "Tổng số lần đo PPG người dùng đã thực hiện.")
	public Object countHealthRecords() {
		if (!isMember()) return notForRole();
		return healthRecordService.countRecords(user.userId());
	}

	@Tool(description = "Thống kê tập luyện của tuần chứa ngày referenceDate (định dạng yyyy-MM-dd): tổng phút, calo, số buổi, phân bố theo ngày. Bỏ trống để lấy tuần hiện tại.")
	public Object getWeeklyWorkoutStats(@ToolParam(description = "ngày bất kỳ trong tuần cần xem, yyyy-MM-dd", required = false) String referenceDate) {
		if (!isMember()) return notForRole();
		return workoutService.getWeeklyStats(user.userId(), dateOrToday(referenceDate), user.timezone());
	}

	@Tool(description = "Hoạt động trong một ngày (yyyy-MM-dd): số bước, phút vận động, calo tiêu hao so với mục tiêu, mức tải tim mạch. Bỏ trống để lấy hôm nay.")
	public Object getDailyActivity(@ToolParam(description = "ngày cần xem, yyyy-MM-dd", required = false) String date) {
		if (!isMember()) return notForRole();
		return workoutService.getDailyActivity(user.userId(), dateOrToday(date), user.timezone());
	}

	@Tool(description = "Các phiên tư vấn với bác sĩ của người dùng, mới nhất trước (tối đa 10): bác sĩ, trạng thái, thời gian bắt đầu và kết thúc.")
	public Object getMyConsultationSessions() {
		if (!isMember()) return notForRole();
		return consultationSessionService.getMySessions(user.userId(), PageRequest.of(0, 10)).getContent();
	}

	@Tool(description = "Lịch sử các đợt chăm sóc đã kết thúc của người dùng kèm tóm tắt của bác sĩ (tối đa 10).")
	public Object getMyCareHistory() {
		if (!isMember()) return notForRole();
		return careHistoryService.getMemberHistory(user.userId(), PageRequest.of(0, 10)).getContent();
	}

	@Tool(description = "Chế độ ăn mà bác sĩ đã kê cho người dùng (nếu có).")
	public Object getMyDietPrescription() {
		if (!isMember()) return notForRole();
		return dietPrescriptionService.get(user.userId());
	}

	private boolean isMember() {
		return user.role() == UserRole.MEMBER;
	}

	private Map<String, String> notForRole() {
		return Map.of("notice", "Dữ liệu này chỉ có với tài khoản hội viên; người dùng hiện tại có vai trò " + user.role());
	}

	private String dateOrToday(String date) {
		if (date != null && !date.isBlank()) return date.trim();
		return LocalDate.now(ZoneId.of(user.timezone())).toString();
	}
}
