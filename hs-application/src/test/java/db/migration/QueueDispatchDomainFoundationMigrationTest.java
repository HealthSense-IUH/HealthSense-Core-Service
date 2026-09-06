package db.migration;

import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.migration.Context;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class QueueDispatchDomainFoundationMigrationTest {

    @Test
    void backfillsLegacyRowsAndPreservesLegacyHistory() throws Exception {
        try (Connection connection = connection("backfill")) {
            createLegacySchema(connection);
            try (Statement statement = connection.createStatement()) {
                statement.execute("INSERT INTO consultation_requests (id, member_id, status) VALUES (1, 10, 'PENDING_REVIEW')");
                statement.execute("INSERT INTO consultation_sessions (id, member_id, doctor_id, status, ends_at) " +
                        "VALUES (2, 10, 20, 'COMPLETED', CURRENT_TIMESTAMP)");
                statement.execute("INSERT INTO doctor_care_profiles (id, doctor_id) VALUES (3, 20)");
                for (String table : legacyHistoryTables())
                    statement.execute("INSERT INTO " + table + " (id) VALUES (100)");
            }

            migrate(connection);
            migrate(connection);

            assertEquals("LEGACY_V3", scalar(connection,
                    "SELECT flow_type FROM consultation_requests WHERE id = 1"));
            assertEquals("LEGACY_V3", scalar(connection,
                    "SELECT flow_type FROM consultation_sessions WHERE id = 2"));
            assertEquals("UNAVAILABLE", scalar(connection,
                    "SELECT dispatch_status FROM doctor_care_profiles WHERE id = 3"));
            assertEquals(Boolean.FALSE, scalar(connection,
                    "SELECT stop_after_current_session FROM doctor_care_profiles WHERE id = 3"));
            for (String table : legacyHistoryTables())
                assertEquals(1L, ((Number) scalar(connection, "SELECT COUNT(*) FROM " + table)).longValue());
        }
    }

    @Test
    void supportsAllLegacyAndQueueRequestStatuses() throws Exception {
        try (Connection connection = connection("request_status")) {
            createLegacySchema(connection);
            migrate(connection);

            List<String> statuses = List.of(
                    "PENDING_REVIEW", "NEED_MORE_INFO", "WAITING_ACCEPTANCE", "WAITING_PAYMENT",
                    "FULFILLED", "REJECTED", "CANCELLED", "EXPIRED", "QUEUED", "TIMED_OUT");
            try (Statement statement = connection.createStatement()) {
                long id = 1;
                for (String status : statuses)
                    statement.execute("INSERT INTO consultation_requests (id, member_id, status) VALUES (" +
                            id++ + ", 10, '" + status + "')");
                assertThrows(SQLException.class, () -> statement.execute(
                        "INSERT INTO consultation_requests (id, member_id, status) VALUES (99, 10, 'UNKNOWN')"));
            }
        }
    }

    @Test
    void enforcesQueueEntryIdentityNumberAndStatusConstraints() throws Exception {
        try (Connection connection = connection("queue_constraints")) {
            createLegacySchema(connection);
            migrate(connection);
            try (Statement statement = connection.createStatement()) {
                for (int id = 1; id <= 8; id++)
                    statement.execute("INSERT INTO consultation_requests (id, member_id, status, flow_type) " +
                            "VALUES (" + id + ", " + (100 + id) + ", 'QUEUED', 'QUEUE_DISPATCH_V1')");

                List<String> statuses = List.of("WAITING", "OFFERING_DOCTOR", "WAITING_MEMBER_CONFIRMATION",
                        "FULFILLED", "CANCELLED", "TIMED_OUT");
                long id = 1;
                for (String status : statuses) {
                    statement.execute("INSERT INTO consultation_queue_entries " +
                            "(id, request_id, member_id, queue_date, queue_number, status, queued_at) VALUES (" +
                            id + ", " + id + ", " + (100 + id) + ", DATE '2026-09-05', " + id +
                            ", '" + status + "', CURRENT_TIMESTAMP)");
                    id++;
                }

                assertThrows(SQLException.class, () -> statement.execute("INSERT INTO consultation_queue_entries " +
                        "(id, request_id, member_id, queue_date, queue_number, status, queued_at) " +
                        "VALUES (20, 1, 200, DATE '2026-09-06', 20, 'WAITING', CURRENT_TIMESTAMP)"));
                assertThrows(SQLException.class, () -> statement.execute("INSERT INTO consultation_queue_entries " +
                        "(id, request_id, member_id, queue_date, queue_number, status, queued_at) " +
                        "VALUES (21, 7, 207, DATE '2026-09-05', 1, 'WAITING', CURRENT_TIMESTAMP)"));
                assertThrows(SQLException.class, () -> statement.execute("INSERT INTO consultation_queue_entries " +
                        "(id, request_id, member_id, queue_date, queue_number, status, queued_at) " +
                        "VALUES (22, 8, 208, DATE '2026-09-05', 8, 'UNKNOWN', CURRENT_TIMESTAMP)"));
            }
        }
    }

    @Test
    void persistsDoctorDispatchStateAndBusySessionOwnership() throws Exception {
        try (Connection connection = connection("doctor_dispatch")) {
            createLegacySchema(connection);
            migrate(connection);
            try (Statement statement = connection.createStatement()) {
                statement.execute("INSERT INTO consultation_sessions " +
                        "(id, member_id, doctor_id, status, ends_at) VALUES (50, 10, 20, 'ACTIVE', CURRENT_TIMESTAMP)");
                statement.execute("INSERT INTO doctor_care_profiles (id, doctor_id) VALUES (60, 20)");

                assertEquals("UNAVAILABLE", scalar(connection,
                        "SELECT dispatch_status FROM doctor_care_profiles WHERE id = 60"));
                assertEquals(Boolean.FALSE, scalar(connection,
                        "SELECT stop_after_current_session FROM doctor_care_profiles WHERE id = 60"));

                statement.execute("UPDATE doctor_care_profiles SET dispatch_status = 'BUSY', " +
                        "busy_session_id = 50, stop_after_current_session = TRUE WHERE id = 60");
                assertEquals(50L, ((Number) scalar(connection,
                        "SELECT busy_session_id FROM doctor_care_profiles WHERE id = 60")).longValue());
                assertThrows(SQLException.class, () -> statement.execute(
                        "UPDATE doctor_care_profiles SET busy_session_id = 999 WHERE id = 60"));
                assertThrows(SQLException.class, () -> statement.execute(
                        "UPDATE doctor_care_profiles SET dispatch_status = 'AVAILABLE' WHERE id = 60"));
            }
        }
    }

    @Test
    void createsQueueCounterSessionAndRoundRobinPointerFoundation() throws Exception {
        try (Connection connection = connection("remaining_foundation")) {
            createLegacySchema(connection);
            migrate(connection);
            try (Statement statement = connection.createStatement()) {
                statement.execute("INSERT INTO consultation_queue_counter (queue_date, last_number) " +
                        "VALUES (DATE '2026-09-05', 37)");
                statement.execute("UPDATE consultation_dispatch_state SET last_doctor_id = 22 WHERE id = 1");
                statement.execute("INSERT INTO consultation_sessions " +
                        "(id, member_id, doctor_id, status, ends_at, flow_type, continuation_round, " +
                        "block_started_at, doctor_released_at, doctor_release_reason) VALUES " +
                        "(70, 10, 20, 'COMPLETED', CURRENT_TIMESTAMP, 'QUEUE_DISPATCH_V1', 4, " +
                        "CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'SUMMARY_FINALIZED')");
            }

            assertEquals(37L, ((Number) scalar(connection,
                    "SELECT last_number FROM consultation_queue_counter WHERE queue_date = DATE '2026-09-05'")).longValue());
            assertEquals(22L, ((Number) scalar(connection,
                    "SELECT last_doctor_id FROM consultation_dispatch_state WHERE id = 1")).longValue());
            assertEquals(4, ((Number) scalar(connection,
                    "SELECT continuation_round FROM consultation_sessions WHERE id = 70")).intValue());
        }
    }

    @Test
    void singletonAndCounterLocksMakeConcurrentFirstDailyAdmissionsUnique() throws Exception {
        String databaseName = "concurrent_first_admission";
        try (Connection keeper = connection(databaseName)) {
            createLegacySchema(keeper);
            migrate(keeper);
            int admissionCount = 8;
            CountDownLatch start = new CountDownLatch(1);
            ExecutorService executor = Executors.newFixedThreadPool(admissionCount);
            List<Future<Long>> futures = java.util.stream.LongStream.rangeClosed(1, admissionCount)
                    .mapToObj(id -> executor.submit((Callable<Long>) () -> {
                        start.await();
                        try (Connection connection = connection(databaseName)) {
                            connection.setAutoCommit(false);
                            try {
                                long number = allocateDailyNumber(connection);
                                try (Statement statement = connection.createStatement()) {
                                    statement.execute("INSERT INTO consultation_requests " +
                                            "(id, member_id, status, flow_type) VALUES (" + id + ", " +
                                            (1000 + id) + ", 'QUEUED', 'QUEUE_DISPATCH_V1')");
                                    statement.execute("INSERT INTO consultation_queue_entries " +
                                            "(id, request_id, member_id, queue_date, queue_number, status, queued_at) " +
                                            "VALUES (" + id + ", " + id + ", " + (1000 + id) +
                                            ", DATE '2026-09-05', " + number + ", 'WAITING', CURRENT_TIMESTAMP)");
                                }
                                connection.commit();
                                return number;
                            } catch (Exception exception) {
                                connection.rollback();
                                throw exception;
                            }
                        }
                    }))
                    .toList();
            start.countDown();
            Set<Long> numbers = futures.stream().map(future -> {
                try {
                    return future.get(10, TimeUnit.SECONDS);
                } catch (Exception exception) {
                    throw new AssertionError(exception);
                }
            }).collect(Collectors.toSet());
            executor.shutdownNow();

            assertEquals(admissionCount, numbers.size());
            assertEquals(java.util.stream.LongStream.rangeClosed(1, admissionCount).boxed().collect(Collectors.toSet()),
                    numbers);
            assertEquals((long) admissionCount, ((Number) scalar(keeper,
                    "SELECT COUNT(*) FROM consultation_queue_entries")).longValue());
            assertEquals((long) admissionCount, ((Number) scalar(keeper,
                    "SELECT last_number FROM consultation_queue_counter WHERE queue_date = DATE '2026-09-05'")).longValue());
        }
    }

    private long allocateDailyNumber(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeQuery("SELECT id FROM consultation_dispatch_state WHERE id = 1 FOR UPDATE").close();
            try (ResultSet result = statement.executeQuery(
                    "SELECT last_number FROM consultation_queue_counter " +
                            "WHERE queue_date = DATE '2026-09-05' FOR UPDATE")) {
                if (!result.next()) {
                    statement.execute("INSERT INTO consultation_queue_counter (queue_date, last_number) " +
                            "VALUES (DATE '2026-09-05', 1)");
                    return 1L;
                }
                long next = result.getLong(1) + 1;
                statement.execute("UPDATE consultation_queue_counter SET last_number = " + next +
                        " WHERE queue_date = DATE '2026-09-05'");
                return next;
            }
        }
    }

    private Connection connection(String name) throws SQLException {
        return DriverManager.getConnection("jdbc:h2:mem:queue_dispatch_" + name +
                ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000");
    }

    private void createLegacySchema(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE consultation_requests (
                        id BIGINT PRIMARY KEY,
                        member_id BIGINT NOT NULL,
                        status VARCHAR(30) NOT NULL,
                        CONSTRAINT consultation_requests_status_check CHECK (status IN (
                            'PENDING_REVIEW', 'NEED_MORE_INFO', 'WAITING_ACCEPTANCE', 'WAITING_PAYMENT',
                            'FULFILLED', 'REJECTED', 'CANCELLED', 'EXPIRED'))
                    )
                    """);
            statement.execute("""
                    CREATE TABLE consultation_sessions (
                        id BIGINT PRIMARY KEY,
                        member_id BIGINT NOT NULL,
                        doctor_id BIGINT NOT NULL,
                        status VARCHAR(30) NOT NULL,
                        ends_at TIMESTAMP WITH TIME ZONE NOT NULL,
                        summary_due_at TIMESTAMP WITH TIME ZONE
                    )
                    """);
            statement.execute("CREATE TABLE doctor_care_profiles (id BIGINT PRIMARY KEY, doctor_id BIGINT NOT NULL UNIQUE)");
            for (String table : legacyHistoryTables())
                statement.execute("CREATE TABLE " + table + " (id BIGINT PRIMARY KEY)");
        }
    }

    private List<String> legacyHistoryTables() {
        return List.of("care_service_packages", "care_service_agreements", "consultation_payments",
                "consultation_refunds", "doctor_reservations");
    }

    private void migrate(Connection connection) throws Exception {
        new V11__queue_dispatch_domain_foundation().migrate(context(connection));
    }

    private Object scalar(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(sql)) {
            assertTrue(result.next());
            return result.getObject(1);
        }
    }

    private Context context(Connection connection) {
        return new Context() {
            @Override
            public Configuration getConfiguration() {
                return null;
            }

            @Override
            public Connection getConnection() {
                return connection;
            }
        };
    }
}
