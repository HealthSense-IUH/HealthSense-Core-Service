package fit.iuh.se.hshealthrecord.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class HealthStatisticsEtlJob {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Chạy mỗi giờ một lần: "0 0 * * * *"
     * Quét dữ liệu ngày hôm nay và hôm qua để tính toán và Upsert vào bảng vật lý.
     * Khởi chạy ngay lập tức 1 lần khi App start (để Backfill).
     */
    @Scheduled(cron = "0 0 * * * *")
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void runIncrementalEtl() {
        log.info("[ETL] Bắt đầu tiến trình cập nhật Thống kê nhịp tim (Incremental Update)...");
        
        String sql = """
            INSERT INTO health_statistics_daily (user_id, stat_date, total_records, total_normal, total_afib, total_uncertain, total_afib_suspected)
            SELECT
                hr.user_id,
                DATE_TRUNC('day', hr.created_at AT TIME ZONE 'UTC' AT TIME ZONE COALESCE(up.timezone, 'Asia/Ho_Chi_Minh')) AS stat_date,
                COUNT(*) AS total_records,
                CAST(SUM(CASE WHEN hr.prediction_label = 'NORMAL' THEN 1 ELSE 0 END) AS INTEGER) AS total_normal,
                CAST(SUM(CASE WHEN hr.prediction_label = 'AFIB' THEN 1 ELSE 0 END) AS INTEGER) AS total_afib,
                CAST(SUM(CASE WHEN hr.prediction_label = 'UNCERTAIN' THEN 1 ELSE 0 END) AS INTEGER) AS total_uncertain,
                CAST(SUM(CASE WHEN hr.prediction_label = 'AFIB_SUSPECTED' THEN 1 ELSE 0 END) AS INTEGER) AS total_afib_suspected
            FROM health_records hr
            LEFT JOIN user_profiles up ON hr.user_id = up.user_id
            WHERE hr.status = 'COMPLETED'
              -- Rolling window: Chỉ quét record tạo trong hôm nay và hôm qua
              -- AND hr.created_at >= (CURRENT_DATE - INTERVAL '1 day')
            GROUP BY 1, 2
            ON CONFLICT (user_id, stat_date) DO UPDATE SET
                total_records = EXCLUDED.total_records,
                total_normal = EXCLUDED.total_normal,
                total_afib = EXCLUDED.total_afib,
                total_uncertain = EXCLUDED.total_uncertain,
                total_afib_suspected = EXCLUDED.total_afib_suspected;
        """;

        int rowsAffected = jdbcTemplate.update(sql);
        log.info("[ETL] Hoàn tất. Đã Upsert {} bản ghi thống kê.", rowsAffected);
    }
}
