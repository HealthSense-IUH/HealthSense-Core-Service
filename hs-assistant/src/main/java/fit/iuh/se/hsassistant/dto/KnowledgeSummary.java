package fit.iuh.se.hsassistant.dto;

/** Số lượng tài liệu theo trạng thái, hiển thị ở đầu trang quản lý kho tri thức. */
public record KnowledgeSummary(long total, long ready, long processing, long failed) {
}
