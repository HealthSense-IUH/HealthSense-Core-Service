package fit.iuh.se.hsoperations.i18n;

import fit.iuh.se.hsshared.i18n.RequestLanguage;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dịch tiêu đề / nội dung thông báo lúc đọc theo ngôn ngữ của request (header {@code lang}).
 * <p>
 * Thông báo lưu đúng câu lúc tạo (phần lớn tiếng Anh, vài câu tiếng Việt), nên dịch bằng bảng câu song ngữ: khớp
 * nguyên câu, hoặc theo mẫu cho câu có số / trạng thái. Áp được cho cả thông báo đã lưu từ trước. Câu không có trong
 * bảng (ví dụ câu mới thêm ở service mà chưa bổ sung vào đây) giữ nguyên.
 */
public final class NotificationText {
    /** Cặp tiếng Anh, tiếng Việt của mọi câu thông báo cố định trong code. */
    private static final List<String[]> PAIRS = List.of(
            // Ví lượt tư vấn
            pair("Consultation credits added", "Đã cộng lượt tư vấn"),
            pair("Consultation credit refunded", "Đã hoàn lượt tư vấn"),
            // Kết quả sức khỏe
            pair("Health result needs attention", "Kết quả sức khỏe cần chú ý"),
            pair("A health result needs clinical attention. This is not an emergency response service; seek local "
                    + "emergency help for urgent symptoms.",
                    "Một kết quả sức khỏe cần được bác sĩ chú ý. Đây không phải dịch vụ cấp cứu; khi có triệu chứng "
                            + "khẩn cấp, hãy gọi cấp cứu tại chỗ."),
            pair("Authorized record needs review", "Bản đo được chia sẻ cần xem xét"),
            pair("An authorized HealthRecord needs clinical review. No immediate-response guarantee is implied.",
                    "Một bản đo được chia sẻ cần bác sĩ xem xét. Không cam kết phản hồi ngay lập tức."),
            pair("Health record shared", "Bản đo đã được chia sẻ"),
            pair("A HealthRecord was authorized for this active care episode.",
                    "Một bản đo đã được chia sẻ cho đợt chăm sóc đang diễn ra."),
            // Thỏa thuận, thanh toán
            pair("Payment required", "Cần thanh toán"),
            pair("Care agreement update", "Cập nhật thỏa thuận chăm sóc"),
            pair("Your agreement was accepted. Complete payment to activate care.",
                    "Thỏa thuận đã được chấp nhận. Hãy thanh toán để kích hoạt chăm sóc."),
            pair("A care agreement is ready for your review and acceptance.",
                    "Thỏa thuận chăm sóc đã sẵn sàng để bạn xem và chấp nhận."),
            pair("The care agreement is no longer valid.", "Thỏa thuận chăm sóc không còn hiệu lực."),
            pair("Payment confirmed", "Đã xác nhận thanh toán"),
            pair("Payment under review", "Thanh toán đang được xem xét"),
            pair("Payment attempt closed", "Lượt thanh toán đã đóng"),
            pair("Your payment was verified.", "Thanh toán của bạn đã được xác minh."),
            pair("Your payment was received and requires operational review. Care was not activated.",
                    "Đã nhận thanh toán và cần bộ phận vận hành xem xét. Chăm sóc chưa được kích hoạt."),
            pair("The payment attempt failed, was cancelled, or expired.",
                    "Lượt thanh toán thất bại, đã hủy hoặc hết hạn."),
            pair("A payment link is available for your accepted care agreement.",
                    "Đã có liên kết thanh toán cho thỏa thuận chăm sóc bạn đã chấp nhận."),
            pair("Payment requires review", "Thanh toán cần xem xét"),
            pair("Verified payment evidence requires coordinator review and did not activate care.",
                    "Bằng chứng thanh toán đã xác minh cần điều phối viên xem xét; chăm sóc chưa được kích hoạt."),
            // Hoàn tiền, gia hạn
            pair("Refund status updated", "Cập nhật trạng thái hoàn tiền"),
            pair("Refund review required", "Cần xem xét hoàn tiền"),
            pair("Paid evidence requires a refund recommendation.", "Khoản đã thanh toán cần đề xuất hoàn tiền."),
            pair("Renewal update", "Cập nhật gia hạn"),
            pair("Your care end date was extended.", "Ngày kết thúc chăm sóc của bạn đã được gia hạn."),
            pair("The care episode end date was extended.", "Ngày kết thúc đợt chăm sóc đã được gia hạn."),
            // Yêu cầu chăm sóc, hàng đợi, điều phối
            pair("Care request queued", "Yêu cầu chăm sóc đã vào hàng đợi"),
            pair("Your care request was received and added to the consultation queue.",
                    "Yêu cầu chăm sóc của bạn đã được nhận và đưa vào hàng đợi tư vấn."),
            pair("More information needed", "Cần bổ sung thông tin"),
            pair("Care request update", "Cập nhật yêu cầu chăm sóc"),
            pair("Care agreement ready", "Thỏa thuận chăm sóc đã sẵn sàng"),
            pair("Care request cancelled", "Yêu cầu chăm sóc đã hủy"),
            pair("Care offer expired", "Đề nghị chăm sóc đã hết hạn"),
            pair("Care request received", "Đã nhận yêu cầu chăm sóc"),
            pair("Your care coordinator requested additional information.",
                    "Điều phối viên đề nghị bạn bổ sung thông tin."),
            pair("Your care request was not approved. Review the request for details.",
                    "Yêu cầu chăm sóc của bạn chưa được duyệt. Xem yêu cầu để biết chi tiết."),
            pair("Your care request was cancelled before activation.",
                    "Yêu cầu chăm sóc của bạn đã bị hủy trước khi kích hoạt."),
            pair("The care offer and payment window expired.", "Đề nghị chăm sóc và thời hạn thanh toán đã hết."),
            pair("Your care request was submitted for review.", "Yêu cầu chăm sóc của bạn đã được gửi để xem xét."),
            pair("New care request", "Yêu cầu chăm sóc mới"),
            pair("Care request resubmitted", "Yêu cầu chăm sóc được gửi lại"),
            pair("A care request is ready for coordinator review.", "Có yêu cầu chăm sóc chờ điều phối viên xem xét."),
            pair("Doctor coordination required", "Cần điều phối bác sĩ"),
            pair("A care request requires a new Doctor reservation.", "Một yêu cầu chăm sóc cần đặt lại bác sĩ."),
            pair("New consultation offer", "Lượt tư vấn mới"),
            pair("A new consultation is waiting. Please confirm within 5 minutes.",
                    "Có một lượt tư vấn mới. Vui lòng xác nhận trong 5 phút."),
            pair("A doctor is ready for your consultation", "Bác sĩ đã sẵn sàng tư vấn"),
            pair("A doctor is ready for your consultation. Please confirm within 15 minutes.",
                    "Bác sĩ đã sẵn sàng tư vấn. Vui lòng xác nhận tham gia trong 15 phút."),
            pair("Consultation offer expired", "Lượt tư vấn đã hết hạn"),
            pair("The time to confirm the consultation has passed. Please create a new request when needed.",
                    "Thời gian xác nhận tham gia tư vấn đã hết. Vui lòng tạo yêu cầu mới khi cần."),
            // Phiên / đợt chăm sóc
            pair("Consultation started", "Phiên tư vấn đã bắt đầu"),
            pair("Your consultation is ready.", "Phiên tư vấn của bạn đã sẵn sàng."),
            pair("The consultation has been activated.", "Phiên tư vấn đã được kích hoạt."),
            pair("Care activated", "Đã kích hoạt chăm sóc"),
            pair("Your care episode is now active.", "Đợt chăm sóc của bạn đã được kích hoạt."),
            pair("Care episode assigned", "Đã nhận đợt chăm sóc"),
            pair("A care episode is now active and available in your care list.",
                    "Một đợt chăm sóc đã được kích hoạt và có trong danh sách chăm sóc của bạn."),
            pair("Care episode update", "Cập nhật đợt chăm sóc"),
            pair("The care episode is now active.", "Đợt chăm sóc đã được kích hoạt."),
            pair("The care episode is ending within 24 hours.", "Đợt chăm sóc sẽ kết thúc trong 24 giờ."),
            pair("The care period has completed.", "Thời gian chăm sóc đã kết thúc."),
            pair("The care episode was administratively closed.", "Đợt chăm sóc đã được quản trị viên đóng."),
            pair("A termination request is awaiting operational review.",
                    "Yêu cầu chấm dứt đang chờ bộ phận vận hành xem xét."),
            pair("The active care episode requires operational review.",
                    "Đợt chăm sóc đang diễn ra cần bộ phận vận hành xem xét."),
            pair("The care episode status changed.", "Trạng thái đợt chăm sóc đã thay đổi."),
            pair("Care episode requires review", "Đợt chăm sóc cần xem xét"),
            pair("An active care episode requires coordinator review.",
                    "Một đợt chăm sóc đang diễn ra cần điều phối viên xem xét."),
            pair("Continue consultation?", "Tiếp tục tư vấn?"),
            pair("Choose whether to continue this consultation.", "Chọn có tiếp tục buổi tư vấn này hay không."),
            pair("Consultation completed", "Buổi tư vấn đã hoàn tất"),
            pair("Your consultation session has completed.", "Phiên tư vấn của bạn đã hoàn tất."),
            pair("New care message", "Tin nhắn chăm sóc mới"),
            pair("You have a new message in an active care episode.",
                    "Bạn có tin nhắn mới trong đợt chăm sóc đang diễn ra."),
            // Tóm tắt cuối
            pair("Final summary required", "Cần viết tóm tắt cuối"),
            pair("Complete the Final Summary before the displayed deadline to finish this consultation lifecycle.",
                    "Hoàn thành tóm tắt cuối trước hạn hiển thị để kết thúc buổi tư vấn."),
            pair("Final care summary available", "Đã có tóm tắt chăm sóc cuối"),
            pair("Your finalized care summary is available in Care History.",
                    "Tóm tắt chăm sóc cuối của bạn đã có trong Lịch sử chăm sóc."),
            pair("Final summary action required", "Cần xử lý tóm tắt cuối"),
            pair("A care episode requires Final Care Summary action.",
                    "Một đợt chăm sóc cần xử lý tóm tắt chăm sóc cuối."),
            pair("Final summary follow-up", "Theo dõi tóm tắt cuối"),
            pair("A care episode requires Final Care Summary follow-up.",
                    "Một đợt chăm sóc cần theo dõi tóm tắt chăm sóc cuối."),
            pair("Final summary deadline expired", "Đã quá hạn tóm tắt cuối"),
            pair("Consultation intake was disabled because the Final Summary was not completed on time. You can still "
                    + "finalize it from session history.",
                    "Bạn tạm ngưng nhận tư vấn vì chưa hoàn thành tóm tắt cuối đúng hạn. Bạn vẫn có thể hoàn tất trong "
                            + "lịch sử phiên."));

    private static final Map<String, String> TO_VIETNAMESE = new HashMap<>();
    private static final Map<String, String> TO_ENGLISH = new HashMap<>();

    static {
        for (String[] pair : PAIRS) {
            if (TO_VIETNAMESE.put(pair[0], pair[1]) != null || TO_ENGLISH.put(pair[1], pair[0]) != null)
                throw new IllegalStateException("Duplicate notification text: " + pair[0]);
        }
    }

    /** Trạng thái hoàn tiền / gia hạn xuất hiện trong câu "... status is now X." */
    private static final Map<String, String> STATUS_VI = Map.ofEntries(
            Map.entry("REVIEW_REQUIRED", "chờ xem xét"), Map.entry("RECOMMENDED", "đã đề xuất"),
            Map.entry("APPROVED", "đã duyệt"), Map.entry("REJECTED", "bị từ chối"),
            Map.entry("PROCESSING", "đang xử lý"), Map.entry("SUCCEEDED", "hoàn tất"), Map.entry("FAILED", "thất bại"),
            Map.entry("REQUESTED", "đã gửi yêu cầu"), Map.entry("UNDER_REVIEW", "đang xem xét"),
            Map.entry("PENDING_ACCEPTANCE", "chờ xác nhận thỏa thuận"), Map.entry("WAITING_PAYMENT", "chờ thanh toán"),
            Map.entry("PAID", "đã thanh toán"), Map.entry("APPLIED", "đã áp dụng"), Map.entry("CANCELLED", "đã hủy"),
            Map.entry("EXPIRED", "đã hết hạn"), Map.entry("REQUIRES_REVIEW", "cần xem xét"));

    /** Câu có phần thay đổi: mẫu tiếng Anh lúc tạo, cách viết lại theo từng ngôn ngữ. */
    private record Template(Pattern pattern, Function<Matcher, String> vietnamese, Function<Matcher, String> english) {
    }

    private static final List<Template> TEMPLATES = List.of(
            new Template(Pattern.compile("(\\d+) consultation credits? (?:were|was) added to your wallet\\."),
                    m -> "Đã cộng " + m.group(1) + " lượt tư vấn vào ví của bạn.", Matcher::group),
            new Template(Pattern.compile("(\\d+) consultation credits? (?:has|have) been returned to your wallet\\."),
                    m -> "Đã hoàn " + m.group(1) + " lượt tư vấn vào ví của bạn.", Matcher::group),
            new Template(Pattern.compile("Your refund status is now ([A-Z_]+)\\."),
                    m -> "Trạng thái hoàn tiền của bạn: " + STATUS_VI.getOrDefault(m.group(1), m.group(1)) + ".",
                    m -> "Your refund status is now " + humanize(m.group(1)) + "."),
            new Template(Pattern.compile("Your renewal status is now ([A-Z_]+)\\."),
                    m -> "Trạng thái gia hạn của bạn: " + STATUS_VI.getOrDefault(m.group(1), m.group(1)) + ".",
                    m -> "Your renewal status is now " + humanize(m.group(1)) + "."));

    private NotificationText() {}

    /** Theo ngôn ngữ của request đang xử lý. */
    public static String localize(String text) {
        return localize(text, RequestLanguage.current());
    }

    public static String localize(String text, Locale locale) {
        if (text == null) return null;
        boolean vietnamese = RequestLanguage.isVietnamese(locale);
        String exact = (vietnamese ? TO_VIETNAMESE : TO_ENGLISH).get(text);
        if (exact != null) return exact;
        for (Template template : TEMPLATES) {
            Matcher matcher = template.pattern().matcher(text);
            if (matcher.matches()) return (vietnamese ? template.vietnamese() : template.english()).apply(matcher);
        }
        return text;
    }

    /** Các câu cố định, để kiểm thử. */
    static List<String[]> pairs() {
        return PAIRS;
    }

    private static String humanize(String status) {
        return status.toLowerCase(Locale.ROOT).replace('_', ' ');
    }

    private static String[] pair(String english, String vietnamese) {
        return new String[]{english, vietnamese};
    }
}
