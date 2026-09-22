# hs-billing — consultation credit foundation

Phases 1–2 provide wallet/reservation operations, member read APIs and mock purchase orders. There is no PayOS integration or automatic queue charging yet.

See [implementation plan](../implementation-plans/consultation-credits/README.md) and [API contract](../implementation-plans/consultation-credits/API_CONTRACT.md).

## Schema and deployment

Flyway `V15__consultation_credit_foundation` lives in hs-application and adds four tables plus an append-only ledger trigger. V16 adds purchase orders/payment attempts with immutable snapshots and payment evidence. They reference existing user_accounts and credit_packages. There is no production seed. Do not create these tables manually with Hibernate before running Flyway.

## Running the mock purchase flow

There is no deployable mock-billing profile or demo catalog seeder. Credit packages are managed through the administration API. Mock payment support is restricted to the automated `test` profile.

Use GET /api/credits/packages, then POST /api/credits/orders with packageId and an Idempotency-Key UUID. A successful mock purchase returns PAID and the current wallet. GET /api/credits/orders and /api/credits/orders/{id} expose the member's purchase history. See the API contract for request/response examples.

Defaults are DISABLED/false. MOCK requires dev/test and rejects prod/production even when dev is also active. The server does not accept a provider or payment-success flag from the client. PAYOS is not enabled until phase 5.

Purchase admission locks member account → creates/looks up order → completion locks order → wallet. Billing does not lock a member account after locking an order/wallet. Keep this order when integrating phase 3. Immutable order snapshots and unique (member,key)/(order,attempt)/(operation,source) provide durable audit and duplicate protection.

CreditPurchaseCompletionService is the only order settlement path; MockCreditPaymentGateway supplies its internal evidence. The completion event is emitted after commit; it is a best-effort in-process event, not a durable notification outbox. Durable notifications remain phase 4. Purchase history/ledger remain durable regardless of event delivery.

Billing uses the application's existing JPA datasource/transaction manager. The application registers `fit.iuh.se.hsbilling.repository` in its JPA repository scan. No Redis, MongoDB or separate transaction manager is introduced.

## Tests

Fast read/authorization tests:

```text
mvn -pl hs-billing -am test
```

HTTP authorization tests use the actual SecurityConfig with mocked billing service. PostgreSQL tests exercise the actual billing service/JPA repositories, Flyway migration and transaction manager; account lookup is stubbed with per-test users.

Set these environment variables explicitly to enable PostgreSQL integration tests:

```text
BILLING_TEST_JDBC_URL=jdbc:postgresql://localhost:5432/healthsense_test
BILLING_TEST_JDBC_USER=...
BILLING_TEST_JDBC_PASSWORD=...
```

Then run:

```text
mvn -pl hs-application -am test -Dtest=ConsultationCredit*Test,CreditPaymentConfigurationTest -Dsurefire.failIfNoSpecifiedTests=false
```

The test requires permission to create/drop a schema. It creates a UUID-named `credit_test_*` schema, pins all test connections to it, creates minimal user fixtures, migrates and validates JPA mappings, then drops only that schema. It never runs the application migration chain against public/business tables. Missing BILLING_TEST_JDBC_URL skips the integration class; connection/migration/test errors with an explicit URL fail the build.

Do not place passwords in command arguments or commit them. Prefer a dedicated test database in CI. A killed JVM may leave its temporary schema; identify the exact schema from test logs before cleaning it up.

The PostgreSQL tests include wallet concurrency, order retries, concurrent purchases, snapshot preservation, database-injected settlement failure, outer rollback, post-commit events, demo catalog repeatability and the real HTTP purchase → wallet → history flow against the isolated schema. Account attributes are stubbed, but findByIdForUpdate takes an actual PostgreSQL account-row lock on the transaction connection.
