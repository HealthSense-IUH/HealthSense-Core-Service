package fit.iuh.se.hshealthrecord.service.scheduler.impl;

import fit.iuh.se.hshealthrecord.service.scheduler.HealthStatisticsScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthStatisticsSchedulerImpl implements HealthStatisticsScheduler {
    private JdbcTemplate jdbcTemplate;

    @Override
    public void refreshMaterializedView() {
        log.info("Refreshing materialized view...");

        jdbcTemplate.execute("EFRESH MATERIALIZED VIEW CONCURRENTLY mv_daily_health_stats");

        log.info("Materialized view refreshed successfully.");
    }
}
