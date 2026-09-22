package fit.iuh.se.hsapplication.billing;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.extra.spring.SpringUtil;
import db.migration.V15__consultation_credit_foundation;
import db.migration.V16__credit_mock_purchase_orders;
import db.migration.V17__consultation_credit_lifecycle;
import db.migration.V18__credit_administration_hardening;
import db.migration.V19__credit_payos_payments;
import db.migration.V20__charge_consultation_credit_on_session_confirmation;
import fit.iuh.se.hsbilling.config.CreditPaymentConfiguration;
import fit.iuh.se.hsbilling.event.CreditPurchaseCompleted;
import fit.iuh.se.hsbilling.payment.MockCreditPaymentGateway;
import fit.iuh.se.hsbilling.payment.CreditPaymentGateway;
import fit.iuh.se.hsbilling.service.PaymentOrderCodeAllocator;
import fit.iuh.se.hsbilling.service.CreditPurchaseService;
import fit.iuh.se.hsbilling.service.CreditPurchaseCompletionService;
import fit.iuh.se.hsbilling.service.impl.CreditPurchaseServiceImpl;
import fit.iuh.se.hsbilling.entity.enums.*;
import fit.iuh.se.hsbilling.dto.*;
import fit.iuh.se.hsbilling.repository.*;
import fit.iuh.se.hsbilling.service.ConsultationCreditService;
import fit.iuh.se.hsbilling.service.impl.ConsultationCreditServiceImpl;
import fit.iuh.se.hsbilling.service.CreditAdministrationService;
import fit.iuh.se.hsbilling.service.impl.CreditAdministrationServiceImpl;
import fit.iuh.se.hsoperations.event.OperationalEventPublisher;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.enums.*;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.*;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.*;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/** Opt in with BILLING_TEST_JDBC_URL/USER/PASSWORD; creates and drops ONLY its own random schema. */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConsultationCreditPostgresTest {
    private AnnotationConfigApplicationContext context;
    private DriverManagerDataSource admin;
    private JdbcTemplate jdbc;
    private Flyway flyway;
    private String schema;
    private ConsultationCreditService credits;
    private CreditPurchaseService purchases;
    private CreditPurchaseCompletionService completion;
    private CreditAdministrationService administration;
    private final List<CreditPurchaseCompleted> purchaseEvents = new CopyOnWriteArrayList<>();
    private TransactionTemplate tx;
    private final AtomicLong sequence = new AtomicLong(100);
    private final Map<Long, UserAccount> accounts = new ConcurrentHashMap<>();

    @BeforeAll
    void start() {
        String url = System.getenv("BILLING_TEST_JDBC_URL");
        Assumptions.assumeTrue(url != null && !url.isBlank(), "Set BILLING_TEST_JDBC_URL for PostgreSQL integration tests");
        assertTrue(url.startsWith("jdbc:postgresql:"), "These tests require PostgreSQL, not H2");
        admin = datasource(url);
        schema = "credit_test_" + UUID.randomUUID().toString().replace("-", "");
        new JdbcTemplate(admin).execute("CREATE SCHEMA " + schema);
        var scoped = datasource(url);
        Properties properties = new Properties();
        properties.setProperty("currentSchema", schema);
        scoped.setConnectionProperties(properties);
        jdbc = new JdbcTemplate(scoped);
        assertEquals(schema, jdbc.queryForObject("select current_schema()", String.class));
        jdbc.execute("CREATE TABLE user_accounts (id BIGINT PRIMARY KEY)");
        jdbc.execute("CREATE TABLE consultation_requests (id BIGINT PRIMARY KEY)");
        jdbc.execute("CREATE TABLE consultation_sessions (id BIGINT PRIMARY KEY)");
        jdbc.execute("CREATE TABLE consultation_payments (id BIGINT PRIMARY KEY, order_code BIGINT UNIQUE)");
        flyway = Flyway.configure().dataSource(scoped).schemas(schema).defaultSchema(schema)
                .locations("classpath:billing-test-no-sql")
                .baselineOnMigrate(true).baselineVersion("14")
                .javaMigrations(new V15__consultation_credit_foundation(), new V16__credit_mock_purchase_orders(),
                        new V17__consultation_credit_lifecycle(), new V18__credit_administration_hardening(),
                        new V19__credit_payos_payments(),
                        new V20__charge_consultation_credit_on_session_confirmation()).load();
        assertEquals(6, flyway.migrate().migrationsExecuted);
        context = new AnnotationConfigApplicationContext();
        context.getEnvironment().setActiveProfiles("test");
        context.getEnvironment().getPropertySources().addFirst(new org.springframework.core.env.MapPropertySource(
                "billing-test", Map.of("app.billing.payment-provider", "MOCK", "app.billing.mock-payment-enabled", "true")));
        context.addApplicationListener(event -> {
            if (event instanceof org.springframework.context.PayloadApplicationEvent<?> payload
                    && payload.getPayload() instanceof CreditPurchaseCompleted completed) purchaseEvents.add(completed);
        });
        context.registerBean(DataSource.class, () -> scoped);
        context.registerBean(UserAccountRepository.class, () -> {
            var users = mock(UserAccountRepository.class);
            when(users.findById(anyLong())).thenAnswer(call -> Optional.ofNullable(accounts.get(call.getArgument(0))));
            when(users.findByIdForUpdate(anyLong())).thenAnswer(call -> {
                Long id = call.getArgument(0);
                // Real PostgreSQL account-row lock on the transaction's connection; only account attributes are stubbed.
                jdbc.queryForList("select id from user_accounts where id=? for update", id);
                return Optional.ofNullable(accounts.get(id));
            });
            return users;
        });
        context.register(TestConfiguration.class);
        context.refresh();
        credits = context.getBean(ConsultationCreditService.class);
        purchases = context.getBean(CreditPurchaseService.class);
        completion = context.getBean(CreditPurchaseCompletionService.class);
        administration = context.getBean(CreditAdministrationService.class);
        tx = new TransactionTemplate(context.getBean(PlatformTransactionManager.class));
    }

    private DriverManagerDataSource datasource(String url) {
        var ds = new DriverManagerDataSource();
        ds.setUrl(url);
        ds.setUsername(System.getenv("BILLING_TEST_JDBC_USER"));
        ds.setPassword(System.getenv("BILLING_TEST_JDBC_PASSWORD"));
        return ds;
    }

    @AfterAll
    void stop() {
        if (context != null) context.close();
        if (admin != null && schema != null && schema.matches("credit_test_[a-f0-9]{32}"))
            new JdbcTemplate(admin).execute("DROP SCHEMA " + schema + " CASCADE");
    }

    @Configuration(proxyBeanMethods = false)
    @EnableTransactionManagement
    @EnableJpaRepositories(basePackageClasses = CreditWalletRepository.class)
    @Import({ConsultationCreditServiceImpl.class, CreditAdministrationServiceImpl.class, CreditPurchaseServiceImpl.class,
            CreditPurchaseCompletionService.class, MockCreditPaymentGateway.class, CreditPaymentConfiguration.class,
            PaymentOrderCodeAllocator.class})
    static class TestConfiguration {
        @Bean Snowflake snowflake() { return new Snowflake(25, 25); }
        @Bean static SpringUtil springUtil() { return new SpringUtil(); }
        @Bean OperationalEventPublisher operationalEventPublisher() { return mock(OperationalEventPublisher.class); }
        @Bean CreditPaymentGateway creditPaymentGateway() { return mock(CreditPaymentGateway.class); }
        @Bean TransactionTemplate transactionTemplate(PlatformTransactionManager manager) {
            return new TransactionTemplate(manager);
        }
        @Bean LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource ds) {
            var factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(ds);
            factory.setPackagesToScan("fit.iuh.se.hsbilling.entity");
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "validate",
                    "hibernate.jdbc.time_zone", "UTC"));
            return factory;
        }
        @Bean PlatformTransactionManager transactionManager(EntityManagerFactory factory) {
            return new JpaTransactionManager(factory);
        }
    }

    private long member() {
        long id = sequence.incrementAndGet();
        jdbc.update("INSERT INTO user_accounts(id) VALUES (?)", id);
        accounts.put(id, UserAccount.builder().id(id).role(UserRole.MEMBER).status(AccountStatus.ACTIVE).build());
        return id;
    }

    private CreditWalletResponse fund(long member, long amount) {
        long order = sequence.incrementAndGet();
        return credits.credit(member, amount, new CreditSource(order), "order-" + order);
    }

    private long entries(long member) {
        return jdbc.queryForObject("""
            SELECT count(*) FROM credit_ledger_entries e JOIN credit_wallets w ON w.id=e.wallet_id
            WHERE w.member_id=?
            """, Long.class, member);
    }

    private void error(ErrorCode code, Runnable action) {
        assertEquals(code, assertThrows(AppException.class, action::run).getErrorCode());
    }

    @Test void migrationRunsOnceAndHibernateValidatesTheSchema() {
        assertEquals(0, flyway.migrate().migrationsExecuted);
        assertNotNull(context.getBean(EntityManagerFactory.class));
        jdbc.update("insert into consultation_requests(id,credit_policy,credit_cost) values (?,?,?)",
                sequence.incrementAndGet(), "PER_SESSION_V1", 1);
        jdbc.update("insert into consultation_requests(id,credit_policy,credit_cost) values (?,?,?)",
                sequence.incrementAndGet(), "PER_SESSION_CONFIRM_V2", 1);
        assertThrows(RuntimeException.class, () -> jdbc.update(
                "insert into consultation_sessions(id,credit_policy,credit_cost) values (?,?,?)",
                sequence.incrementAndGet(), "PER_SESSION_V1", 0));
    }

    @Test void readingMissingWalletDoesNotCreateRowsAndInsufficientReserveRollsBack() {
        long member = member();
        assertEquals(new CreditWalletResponse(0, 0, 0), credits.getWallet(member));
        assertTrue(credits.getLedger(member, PageRequest.of(0, 10)).getContent().isEmpty());
        error(ErrorCode.INSUFFICIENT_CONSULTATION_CREDITS, () -> credits.reserve(member, sequence.incrementAndGet(), 1));
        assertEquals(0L, jdbc.queryForObject("select count(*) from credit_wallets where member_id=?", Long.class, member));
        assertEquals(0, entries(member));
    }

    @Test void reserveCaptureRetryAndTransitions() {
        long member = member(), request = sequence.incrementAndGet(), session = sequence.incrementAndGet();
        fund(member, 5);
        assertEquals(new CreditWalletResponse(5, 1, 4), credits.reserve(member, request, 1).wallet());
        credits.reserve(member, request, 1);
        assertEquals(new CreditWalletResponse(4, 0, 4), credits.capture(member, request, session).wallet());
        credits.capture(member, request, session);
        credits.reserve(member, request, 1);
        assertEquals(3, entries(member));
        error(ErrorCode.INVALID_CREDIT_RESERVATION_STATUS, () -> credits.release(member, request, "cancelled"));
        error(ErrorCode.CREDIT_IDEMPOTENCY_CONFLICT, () -> credits.capture(member, request, session + 1));
        error(ErrorCode.CREDIT_IDEMPOTENCY_CONFLICT, () -> credits.reserve(member, request, 2));
    }

    @Test void v2AdmissionDoesNotReserveAndSessionChargeIsIdempotent() {
        long member = member(), request = sequence.incrementAndGet(), session = sequence.incrementAndGet();
        fund(member, 2);

        credits.requireAvailable(member, 1);
        assertEquals(new CreditWalletResponse(2, 0, 2), credits.getWallet(member));
        assertEquals(0L, jdbc.queryForObject(
                "select count(*) from credit_reservations where request_id=?", Long.class, request));

        assertEquals(new CreditWalletResponse(1, 0, 1),
                credits.chargeSession(member, request, session, 1));
        assertEquals(new CreditWalletResponse(1, 0, 1),
                credits.chargeSession(member, request, session, 1));
        assertEquals(1L, jdbc.queryForObject("""
                select count(*) from credit_ledger_entries
                where operation='SESSION_CHARGE' and source_type='CONSULTATION_SESSION' and source_id=?
                """, Long.class, session));
        assertEquals(0L, jdbc.queryForObject(
                "select count(*) from credit_reservations where request_id=?", Long.class, request));
    }

    @Test void v2ChargeCanBeRefundedWithoutAReservation() {
        long member = member(), request = sequence.incrementAndGet(), session = sequence.incrementAndGet();
        fund(member, 1);
        credits.requireAvailable(member, 1);
        credits.chargeSession(member, request, session, 1);

        var refund = administration.refundCapturedSession(901L, UserRole.ADMIN, member, session,
                1, "service recovery", "refund-v2-" + session);

        assertEquals(new CreditWalletResponse(1, 0, 1), refund.wallet());
        assertEquals(CreditOperation.SESSION_REFUND, refund.entry().operation());
        assertEquals(1L, jdbc.queryForObject(
                "select count(*) from credit_ledger_entries where operation='SESSION_REFUND' and source_id=?",
                Long.class, session));
    }

    @Test void releaseRetryAndDeactivatedMemberCleanup() {
        long member = member(), request = sequence.incrementAndGet();
        fund(member, 5);
        credits.reserve(member, request, 1);
        accounts.get(member).setStatus(AccountStatus.INACTIVE);
        assertEquals(new CreditWalletResponse(5, 0, 5), credits.release(member, request, "timeout").wallet());
        credits.release(member, request, "timeout");
        error(ErrorCode.CREDIT_IDEMPOTENCY_CONFLICT, () -> credits.release(member, request, "different reason"));
        accounts.get(member).setStatus(AccountStatus.ACTIVE);
        error(ErrorCode.INVALID_CREDIT_RESERVATION_STATUS, () -> credits.capture(member, request, 999L));
        assertEquals(3, entries(member));
    }

    @Test void fundingIsIdempotentAndPurchaseSourceCannotBeReusedWithAnotherKey() {
        long member = member(), other = member(), order = sequence.incrementAndGet();
        String key = "purchase-" + order;
        credits.credit(member, 5, new CreditSource(order), key);
        credits.credit(member, 5, new CreditSource(order), key);
        error(ErrorCode.CREDIT_IDEMPOTENCY_CONFLICT, () -> credits.credit(member, 6, new CreditSource(order), key));
        error(ErrorCode.CREDIT_IDEMPOTENCY_CONFLICT, () -> credits.credit(other, 5, new CreditSource(order), key));
        error(ErrorCode.CREDIT_IDEMPOTENCY_CONFLICT, () -> credits.credit(member, 5, new CreditSource(order), key + "new"));
        error(ErrorCode.CREDIT_IDEMPOTENCY_CONFLICT, () -> credits.credit(member, 5, new CreditSource(order + 1), key));
        assertEquals(1, entries(member));
        assertEquals(new CreditWalletResponse(5, 0, 5), credits.getWallet(member));
        assertEquals(new CreditWalletResponse(0, 0, 0), credits.getWallet(other));
    }

    @Test void concurrentFirstFundingDoesNotLoseUpdates() throws Exception {
        long member = member();
        concurrent(() -> fund(member, 5), () -> fund(member, 7));
        assertEquals(new CreditWalletResponse(12, 0, 12), credits.getWallet(member));
        assertEquals(2, entries(member));
    }

    @Test void concurrentDuplicateFundingCreditsOnlyOnce() throws Exception {
        long member = member(), order = sequence.incrementAndGet();
        Supplier<Object> task = () -> credits.credit(member, 5, new CreditSource(order), "same-" + order);
        concurrent(task, task);
        assertEquals(new CreditWalletResponse(5, 0, 5), credits.getWallet(member));
        assertEquals(1, entries(member));
    }

    @Test void onlyOneConcurrentReservationCanTakeTheLastCredit() throws Exception {
        long member = member();
        fund(member, 1);
        Supplier<Object> task = () -> {
            try { credits.reserve(member, sequence.incrementAndGet(), 1); return "held"; }
            catch (AppException ex) { assertEquals(ErrorCode.INSUFFICIENT_CONSULTATION_CREDITS, ex.getErrorCode()); return "insufficient"; }
        };
        assertEquals(Set.of("held", "insufficient"), new HashSet<>(concurrent(task, task)));
        assertEquals(new CreditWalletResponse(1, 1, 0), credits.getWallet(member));
        assertEquals(2, entries(member));
    }

    @Test void outerTransactionRollbackIncludesWalletLedgerAndReservation() {
        long member = member(), request = sequence.incrementAndGet();
        assertThrows(IllegalStateException.class, () -> tx.execute(status -> {
            fund(member, 5);
            credits.reserve(member, request, 1);
            credits.capture(member, request, sequence.incrementAndGet());
            throw new IllegalStateException("simulate session failure");
        }));
        assertEquals(new CreditWalletResponse(0, 0, 0), credits.getWallet(member));
        assertEquals(0, entries(member));
        assertEquals(0L, jdbc.queryForObject("select count(*) from credit_reservations where request_id=?", Long.class, request));
    }

    @Test void postgresConstraintsAndImmutableLedgerCannotBeBypassed() {
        long member = member();
        fund(member, 1);
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class,
                () -> jdbc.update("update credit_wallets set reserved=2 where member_id=?", member));
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class,
                () -> jdbc.update("update credit_wallets set balance=-1 where member_id=?", member));
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> jdbc.update("""
                UPDATE credit_ledger_entries SET reason='tampered'
                WHERE wallet_id=(SELECT id FROM credit_wallets WHERE member_id=?)
                """, member));
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> jdbc.update("""
                DELETE FROM credit_ledger_entries WHERE wallet_id=(SELECT id FROM credit_wallets WHERE member_id=?)
                """, member));
    }

    @Test void negativeAdjustmentUsesOnlyAvailableAndPreservesHeldCredits() {
        long member = member(), request = sequence.incrementAndGet();
        fund(member, 3);
        credits.reserve(member, request, 2);
        error(ErrorCode.INSUFFICIENT_CONSULTATION_CREDITS, () -> administration.adjust(
                900L, UserRole.ADMIN, member, -2, "manual correction", "adjust-too-large-" + member));
        assertEquals(new CreditWalletResponse(3, 2, 1), credits.getWallet(member));
        var result = administration.adjust(900L, UserRole.ADMIN, member, -1,
                "manual correction", "adjust-ok-" + member);
        assertEquals(new CreditWalletResponse(2, 2, 0), result.wallet());
        var retry = administration.adjust(900L, UserRole.ADMIN, member, -1,
                "manual correction", "adjust-ok-" + member);
        assertEquals(result.entry().id(), retry.entry().id());
        assertEquals(result.wallet(), retry.wallet());
    }

    @Test void concurrentAdminsCanRefundCapturedSessionOnlyOnce() throws Exception {
        long member = member(), request = sequence.incrementAndGet(), session = sequence.incrementAndGet();
        fund(member, 2); credits.reserve(member, request, 1); credits.capture(member, request, session);
        Supplier<Object> first = () -> administration.refundCapturedSession(901L, UserRole.ADMIN, member, session,
                1, "service recovery", "refund-a-" + session);
        Supplier<Object> second = () -> administration.refundCapturedSession(902L, UserRole.SUPER_ADMIN, member, session,
                1, "service recovery", "refund-b-" + session);
        concurrent(first, second);
        assertEquals(new CreditWalletResponse(2, 0, 2), credits.getWallet(member));
        assertEquals(1L, jdbc.queryForObject("select count(*) from credit_ledger_entries where operation='SESSION_REFUND' and source_id=?", Long.class, session));
        var check = administration.reconcile(UserRole.ADMIN, member);
        assertTrue(check.consistent());
        assertEquals(check.walletBalance(), check.ledgerBalance());
    }

    @Test void adjustmentCompetingWithReserveKeepsWalletInvariant() throws Exception {
        long member = member(), request = sequence.incrementAndGet(); fund(member, 1);
        Supplier<Object> reserve = () -> { try { credits.reserve(member, request, 1); return "reserved"; }
            catch (AppException ex) { assertEquals(ErrorCode.INSUFFICIENT_CONSULTATION_CREDITS, ex.getErrorCode()); return "reserve-rejected"; } };
        Supplier<Object> adjust = () -> { try { administration.adjust(700L, UserRole.ADMIN, member, -1,
                "approved correction", "race-adjust-" + member); return "adjusted"; }
            catch (AppException ex) { assertEquals(ErrorCode.INSUFFICIENT_CONSULTATION_CREDITS, ex.getErrorCode()); return "adjust-rejected"; } };
        var results = new HashSet<>(concurrent(reserve, adjust));
        assertTrue(results.equals(Set.of("reserved", "adjust-rejected"))
                || results.equals(Set.of("reserve-rejected", "adjusted")));
        var wallet = credits.getWallet(member);
        assertTrue(wallet.balance() >= 0 && wallet.reserved() >= 0 && wallet.reserved() <= wallet.balance());
        assertTrue(administration.reconcile(UserRole.ADMIN, member).consistent());
    }

    @Test void packageAdministrationDefaultsInactiveAndRejectsStaleVersion() {
        var created = administration.createPackage(800L, UserRole.ADMIN,
                new AdminCreditPackageRequest("ADMIN_TEST_" + sequence.incrementAndGet(), "Admin fixture", null, 3L, 3000L, CreditPackageStatus.ACTIVE, null));
        assertEquals(CreditPackageStatus.INACTIVE, created.status());
        var updated = administration.updatePackage(800L, UserRole.ADMIN, Long.valueOf(created.id()),
                new AdminCreditPackageRequest(null, "Updated fixture", null, null, 4000L, CreditPackageStatus.ACTIVE, created.version()));
        assertEquals(CreditPackageStatus.ACTIVE, updated.status());
        assertEquals(4000L, updated.priceVnd());
        error(ErrorCode.CREDIT_PACKAGE_VERSION_CONFLICT, () -> administration.updatePackage(801L, UserRole.SUPER_ADMIN,
                Long.valueOf(created.id()), new AdminCreditPackageRequest(null, "Stale", null, null, null, null, created.version())));
    }

    @Test void adminOrderFiltersIncludeProviderAndPreserveSnapshot() {
        long member = member(), pack = purchasePackage(4);
        var paid = purchases.createOrder(member, pack, "admin-search-" + member);
        var page = administration.getOrders(UserRole.ADMIN, member, CreditOrderStatus.PAID,
                CreditPaymentProvider.MOCK, null, null, PageRequest.of(0, 10));
        assertTrue(page.getContent().stream().anyMatch(o -> o.id().equals(paid.order().id())));
        var detail = administration.getOrder(UserRole.SUPER_ADMIN, Long.valueOf(paid.order().id()));
        assertEquals(Long.toString(member), detail.memberId());
        assertEquals(1, detail.attempts().size());
        assertEquals(CreditPaymentProvider.MOCK, detail.attempts().getFirst().provider());
    }

    @Test void invalidInputsAndOverflowDoNotChangeBalance() {
        long member = member();
        error(ErrorCode.INVALID_PARAMETER, () -> fund(member, 0));
        error(ErrorCode.INVALID_PARAMETER, () -> credits.reserve(member, -1L, 1));
        error(ErrorCode.CREDIT_RESERVATION_NOT_FOUND, () -> credits.capture(member, 1L, 2L));
        fund(member, Long.MAX_VALUE);
        error(ErrorCode.CREDIT_BALANCE_OVERFLOW, () -> fund(member, 1));
        assertEquals(Long.MAX_VALUE, credits.getWallet(member).balance());
        assertEquals(1, entries(member));
    }

    @Test void historyIsOwnedBoundedAndOrderedWithStableTieBreaker() {
        long member = member(), other = member();
        fund(member, 5); fund(other, 7);
        credits.reserve(member, sequence.incrementAndGet(), 1);
        var first = credits.getLedger(member, PageRequest.of(0, 1));
        var second = credits.getLedger(member, PageRequest.of(1, 1));
        assertEquals(2, first.getTotalElements());
        assertNotEquals(first.getContent().getFirst().id(), second.getContent().getFirst().id());
        assertEquals("RESERVE", first.getContent().getFirst().operation().name());
        error(ErrorCode.INVALID_PARAMETER, () -> credits.getLedger(member, PageRequest.of(0, 101)));
        long request = sequence.incrementAndGet();
        credits.reserve(member, request, 1);
        error(ErrorCode.CREDIT_IDEMPOTENCY_CONFLICT, () -> credits.reserve(other, request, 1));
    }

    @Test void serviceRejectsNonMembersAndInactiveAccounts() {
        long id = member();
        for (var role : UserRole.values()) {
            if (role == UserRole.MEMBER) continue;
            accounts.get(id).setRole(role);
            error(ErrorCode.ACCESS_DENIED, () -> credits.getWallet(id));
            error(ErrorCode.ACCESS_DENIED, () -> credits.getPackages(id));
            error(ErrorCode.ACCESS_DENIED, () -> credits.getLedger(id, PageRequest.of(0, 10)));
        }
        accounts.get(id).setRole(UserRole.MEMBER);
        accounts.get(id).setStatus(AccountStatus.INACTIVE);
        error(ErrorCode.ACCOUNT_DISABLED, () -> credits.getWallet(id));
        error(ErrorCode.ACCOUNT_DISABLED, () -> fund(id, 1));
        error(ErrorCode.UNAUTHORIZED, () -> credits.getWallet(null));
    }

    @Test void packageFixtureOnlyExposesActivePackagesInStableOrder() {
        long member = member();
        for (int q : new int[]{1, 5, 10}) {
            jdbc.update("""
                INSERT INTO credit_packages(id,code,name,credit_quantity,price_vnd,status)
                VALUES (?,?,?,?,?,?)
                """, sequence.incrementAndGet(), "TEST_" + q, "Test-only package " + q, q, q * 1000, q == 10 ? "INACTIVE" : "ACTIVE");
        }
        assertEquals(List.of(1L, 5L), credits.getPackages(member).stream()
                .filter(p -> p.code().startsWith("TEST_")).map(CreditPackageResponse::creditQuantity).toList());
    }

    private long purchasePackage(long quantity) {
        long id = sequence.incrementAndGet();
        jdbc.update("""
                INSERT INTO credit_packages(id,code,name,credit_quantity,price_vnd,status)
                VALUES (?,?,?,?,?,'ACTIVE')
                """, id, "BUY_TEST_" + id, "Mock fixture " + id, quantity, quantity * 1000);
        return id;
    }

    private long orderCount(long member) {
        return jdbc.queryForObject("select count(*) from credit_purchase_orders where member_id=?", Long.class, member);
    }

    @Test void mockPurchaseSettlesExactlyOnceAndPublishesOnlyAfterCommit() {
        long member = member(), pack = purchasePackage(5);
        var result = tx.execute(status -> {
            var created = purchases.createOrder(member, pack, "first-buy");
            assertTrue(purchaseEvents.stream().noneMatch(e -> e.memberId().equals(member)));
            return created;
        });
        assertNotNull(result);
        assertEquals(CreditOrderStatus.PAID, result.order().status());
        assertEquals(CreditPaymentProvider.MOCK, result.payment().provider());
        assertEquals(CreditPaymentStatus.PAID, result.payment().status());
        assertEquals(new CreditWalletResponse(5, 0, 5), result.wallet());
        assertNull(result.payment().checkoutUrl());
        assertEquals(result.order().id(), purchases.createOrder(member, pack, "first-buy").order().id());
        completion.completePurchase(Long.valueOf(result.order().id()), Long.valueOf(result.payment().attemptId()),
                new VerifiedCreditPayment(CreditPaymentProvider.MOCK, "mock:" + result.payment().attemptId(), 5000, "VND"));
        assertEquals(1, entries(member));
        assertEquals(1, orderCount(member));
        assertEquals(1, purchaseEvents.stream().filter(e -> e.memberId().equals(member)).count());
        assertEquals(1L, jdbc.queryForObject("select count(*) from credit_payment_attempts where order_id=?", Long.class,
                Long.valueOf(result.order().id())));
    }

    @Test void concurrentPurchaseRetriesUseOneOrderAndOneCredit() throws Exception {
        long member = member(), pack = purchasePackage(5);
        Supplier<Object> task = () -> purchases.createOrder(member, pack, "same-click").order().id();
        assertEquals(1, new HashSet<>(concurrent(task, task)).size());
        assertEquals(1, orderCount(member));
        assertEquals(1, entries(member));
        assertEquals(5, credits.getWallet(member).balance());
    }

    @Test void independentPurchasesAccumulateAndKeyIsScopedToMember() throws Exception {
        long member = member(), other = member(), pack = purchasePackage(5);
        concurrent(() -> purchases.createOrder(member, pack, "click-a"), () -> purchases.createOrder(member, pack, "click-b"));
        purchases.createOrder(other, pack, "click-a");
        assertEquals(10, credits.getWallet(member).balance());
        assertEquals(5, credits.getWallet(other).balance());
        assertEquals(2, orderCount(member));
        assertEquals(2, entries(member));
    }

    @Test void changedPayloadConflictsAndSnapshotSurvivesPackageEditsAndDeactivation() {
        long member = member(), pack = purchasePackage(5), otherPack = purchasePackage(1);
        var original = purchases.createOrder(member, pack, "snapshot");
        jdbc.update("update credit_packages set price_vnd=90000,credit_quantity=9,name='changed',status='INACTIVE' where id=?", pack);
        var retried = purchases.createOrder(member, pack, "snapshot");
        assertEquals(original.order().id(), retried.order().id());
        assertEquals(original.order().amountVnd(), retried.order().amountVnd());
        assertEquals(original.order().creditQuantity(), retried.order().creditQuantity());
        assertEquals(original.order().packageName(), retried.order().packageName());
        error(ErrorCode.CREDIT_IDEMPOTENCY_CONFLICT, () -> purchases.createOrder(member, otherPack, "snapshot"));
        error(ErrorCode.CREDIT_PACKAGE_UNAVAILABLE, () -> purchases.createOrder(member, pack, "new-click"));
        error(ErrorCode.CREDIT_PACKAGE_UNAVAILABLE, () -> purchases.createOrder(member, Long.MAX_VALUE, "missing"));
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class,
                () -> jdbc.update("update credit_purchase_orders set amount_vnd=1 where id=?", Long.valueOf(original.order().id())));
        assertEquals(1, orderCount(member));
    }

    @Test void ownershipRoleAndInputChecksProtectPurchases() {
        long member = member(), other = member(), pack = purchasePackage(1);
        var order = purchases.createOrder(member, pack, "owned");
        error(ErrorCode.CREDIT_ORDER_NOT_FOUND, () -> purchases.getOrder(other, Long.valueOf(order.order().id())));
        assertTrue(purchases.getOrders(other, PageRequest.of(0, 10)).getContent().isEmpty());
        assertEquals(1, purchases.getOrders(member, PageRequest.of(0, 1)).getTotalElements());
        for (String invalid : List.of("", "with space", "x".repeat(129)))
            error(ErrorCode.INVALID_PARAMETER, () -> purchases.createOrder(member, pack, invalid));
        for (var role : UserRole.values()) {
            if (role == UserRole.MEMBER) continue;
            accounts.get(other).setRole(role);
            error(ErrorCode.ACCESS_DENIED, () -> purchases.createOrder(other, pack, "forbidden"));
            error(ErrorCode.ACCESS_DENIED, () -> purchases.getOrders(other, PageRequest.of(0, 10)));
        }
        accounts.get(other).setRole(UserRole.MEMBER);
        accounts.get(other).setStatus(AccountStatus.INACTIVE);
        error(ErrorCode.ACCOUNT_DISABLED, () -> purchases.createOrder(other, pack, "inactive"));
    }

    @Test void invalidOrPayosEvidenceCannotUseMockSettlement() {
        long member = member(), pack = purchasePackage(1);
        var order = purchases.createOrder(member, pack, "evidence");
        long orderId = Long.parseLong(order.order().id()), attemptId = Long.parseLong(order.payment().attemptId());
        for (var evidence : List.of(
                new VerifiedCreditPayment(CreditPaymentProvider.PAYOS, "mock:" + attemptId, 1000, "VND"),
                new VerifiedCreditPayment(CreditPaymentProvider.MOCK, "fake", 1000, "VND"),
                new VerifiedCreditPayment(CreditPaymentProvider.MOCK, "mock:" + attemptId, 999, "VND"),
                new VerifiedCreditPayment(CreditPaymentProvider.MOCK, "mock:" + attemptId, 1000, "USD"))) {
            error(ErrorCode.INVALID_CREDIT_PAYMENT, () -> completion.completePurchase(orderId, attemptId, evidence));
        }
        error(ErrorCode.INVALID_CREDIT_PAYMENT, () -> completion.completePurchase(orderId, -1L,
                new VerifiedCreditPayment(CreditPaymentProvider.MOCK, "mock:-1", 1000, "VND")));
        long pendingOrder = sequence.incrementAndGet(), payosAttempt = sequence.incrementAndGet();
        jdbc.update("""
                INSERT INTO credit_purchase_orders(id,member_id,package_id,package_code,package_name,
                    credit_quantity,amount_vnd,currency,status,idempotency_key,request_fingerprint)
                VALUES (?,?,?,'future-payos','fixture',1,1000,'VND','PENDING_PAYMENT','future-payos','fixture')
                """, pendingOrder, member, pack);
        jdbc.update("""
                INSERT INTO credit_payment_attempts(id,order_id,attempt_number,provider,status,order_code,expires_at)
                VALUES (?,?,1,'PAYOS','PENDING',?,CURRENT_TIMESTAMP + INTERVAL '15 minutes')
                """, payosAttempt, pendingOrder, sequence.incrementAndGet());
        error(ErrorCode.INVALID_CREDIT_PAYMENT, () -> completion.completePurchase(pendingOrder, payosAttempt,
                new VerifiedCreditPayment(CreditPaymentProvider.MOCK, "mock:" + payosAttempt, 1000, "VND")));
        assertEquals("PENDING_PAYMENT", jdbc.queryForObject("select status from credit_purchase_orders where id=?", String.class, pendingOrder));
        assertEquals(1, entries(member));
    }

    @Test void failureWritingPaidOrderRollsBackOrderAttemptWalletLedgerAndEvent() {
        long member = member(), pack = purchasePackage(5);
        jdbc.execute("""
                CREATE FUNCTION fail_test_credit_settlement() RETURNS trigger LANGUAGE plpgsql AS $body$
                BEGIN
                    IF NEW.member_id = %d AND NEW.status='PAID' THEN
                        RAISE EXCEPTION 'Injected settlement failure' USING ERRCODE='23514';
                    END IF;
                    RETURN NEW;
                END; $body$
                """.formatted(member));
        jdbc.execute("CREATE TRIGGER test_fail_settlement BEFORE UPDATE ON credit_purchase_orders FOR EACH ROW EXECUTE FUNCTION fail_test_credit_settlement()");
        try {
            assertThrows(org.springframework.dao.DataAccessException.class, () -> purchases.createOrder(member, pack, "failed"));
            assertEquals(0, orderCount(member));
            assertEquals(0, entries(member));
            assertEquals(0, credits.getWallet(member).balance());
            assertTrue(purchaseEvents.stream().noneMatch(e -> e.memberId().equals(member)));
        } finally {
            jdbc.execute("DROP TRIGGER test_fail_settlement ON credit_purchase_orders");
            jdbc.execute("DROP FUNCTION fail_test_credit_settlement()");
        }
        assertEquals(5, purchases.createOrder(member, pack, "failed").wallet().balance());
    }

    @Test void callerRollbackAlsoRollsBackMockPurchase() {
        long member = member(), pack = purchasePackage(5);
        assertThrows(IllegalStateException.class, () -> tx.execute(status -> {
            purchases.createOrder(member, pack, "outer-rollback");
            throw new IllegalStateException("caller failed");
        }));
        assertEquals(0, orderCount(member));
        assertEquals(0, entries(member));
        assertEquals(0, credits.getWallet(member).balance());
        assertTrue(purchaseEvents.stream().noneMatch(e -> e.memberId().equals(member)));
    }

    @Test void realPurchaseHttpFlowWritesTheIsolatedDatabase() throws Exception {
        long member = member(), pack = purchasePackage(5);
        var principal = fit.iuh.se.hsapplication.dto.auth.UserAuthentication.builder().userId(member).role(UserRole.MEMBER).build();
        var authentication = org.springframework.security.authentication.UsernamePasswordAuthenticationToken.authenticated(
                principal, null, List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_MEMBER")));
        var mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(
                new fit.iuh.se.hsapplication.controller.billing.CreditPurchaseController(purchases),
                new fit.iuh.se.hsapplication.controller.billing.ConsultationCreditController(credits))
                .setCustomArgumentResolvers(new org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new fit.iuh.se.hsshared.advice.handler.GlobalExceptionHandler()).build();
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);
        try {
            mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/credits/orders")
                            .header("Idempotency-Key", "http-buy").contentType("application/json").content("{\"packageId\":\"" + pack + "\"}"))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.order.status").value("PAID"))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.wallet.balance").value(5));
            mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/credits/wallet"))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.available").value(5));
            mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/credits/orders"))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.totalElements").value(1));
            mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/credits/ledger"))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.content[0].operation").value("PURCHASE"));
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
        }
    }

    private List<Object> concurrent(Supplier<?> first, Supplier<?> second) throws Exception {
        try (var pool = Executors.newFixedThreadPool(2)) {
            var ready = new CountDownLatch(2);
            var start = new CountDownLatch(1);
            List<Future<?>> futures = new ArrayList<>();
            for (var task : List.of(first, second)) futures.add(pool.submit(() -> {
                ready.countDown();
                if (!start.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("start timeout");
                return task.get();
            }));
            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();
            List<Object> results = new ArrayList<>();
            for (var future : futures) results.add(future.get(30, TimeUnit.SECONDS));
            return results;
        }
    }
}
