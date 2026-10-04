package fit.iuh.se.hsshared.statistics;

public enum StatisticsGroupBy {
    DAY("day"),
    WEEK("week"),
    MONTH("month");

    private final String sqlUnit;

    StatisticsGroupBy(String sqlUnit) {
        this.sqlUnit = sqlUnit;
    }

    public String sqlUnit() {
        return sqlUnit;
    }
}
