package fit.iuh.se.hshealthrecord.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer {
    private final JdbcTemplate jdbcTemplate;


    @EventListener(ApplicationReadyEvent.class)
    public void initMaterializedView() {
        log.info("Checking and Dropping obsolete materialized view...");

        String dropViewSql = "DROP MATERIALIZED VIEW IF EXISTS mv_daily_health_stats CASCADE";
        String dropTableSql = "DROP TABLE IF EXISTS mv_daily_health_stats CASCADE";

        assert jdbcTemplate != null;
        try {
            jdbcTemplate.execute(dropViewSql);
            jdbcTemplate.execute(dropTableSql);
            log.info("Obsolete Materialized View cleaned up.");
        } catch (Exception e) {
            log.warn("Error dropping obsolete view/table: {}", e.getMessage());
        }
    }
}
