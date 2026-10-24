package fit.iuh.se.hsassistant.dto;

import fit.iuh.se.hsuser.entity.enums.UserRole;

/**
 * Người đang trò chuyện với trợ lý. Tách khỏi principal của lớp web để module này không phụ thuộc hs-application.
 *
 * @param userId   id người dùng (mọi tool chỉ đọc dữ liệu của đúng id này)
 * @param role     vai trò, quyết định bộ tool và danh sách trang đưa vào prompt
 * @param language "vi" hoặc "en", lấy từ header lang của request
 * @param timezone múi giờ hiển thị, ví dụ Asia/Ho_Chi_Minh
 */
public record AssistantUser(Long userId, UserRole role, String language, String timezone) {

	public static AssistantUser of(Long userId, UserRole role, String language, String timezone) {
		String lang = (language == null || language.isBlank()) ? "vi" : language.trim().toLowerCase();
		String tz = (timezone == null || timezone.isBlank()) ? "Asia/Ho_Chi_Minh" : timezone;
		return new AssistantUser(userId, role, lang.startsWith("en") ? "en" : "vi", tz);
	}
}
