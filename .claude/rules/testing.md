---
paths:
  - "src/test/**/*.java"
---

# Testing Rules

- Use JUnit 5 and current project test conventions.
- Use unit tests for pure Java rules such as `OrderStatus.canTransitionTo`.
- Use PostgreSQL Testcontainers for MyBatis SQL, transaction rollback, constraints, ownership, and concurrency.
- Never use a production, public, or shared development database in tests.
- Keep fixtures small, explicit, and deterministic.
- Verify database state after both success and failure.
- Security regression tests must prove a different buyer or supplier cannot access the resource.
- Transaction tests must prove all earlier writes roll back after a later failure.
- Concurrency tests must use separate transactions and synchronization primitives, not arbitrary long sleeps.
- Run the targeted test class before the full suite.
- Name PostgreSQL integration tests `*IT`; Maven Failsafe runs them during `verify`, which requires Docker.
- Use `./mvnw.cmd test` for unit tests and `./mvnw.cmd verify` for the complete unit and integration suite.
- Do not disable, weaken, or delete a failing test without explaining why the expected behavior changed.
- Report the exact command and actual result.
