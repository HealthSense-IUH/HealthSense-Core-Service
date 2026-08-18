package fit.iuh.se.hshealthrecord.repository;

public interface HealthStatProjection {
    Double getStatGroup(); // Dùng Double vì kết quả của EXTRACT trong PostgreSQL trả về kiểu double precision
    Integer getNormalCount();
    Integer getAfibRiskCount();
    Integer getUncertainCount();
    Integer getAfibSuspectedCount();
}
