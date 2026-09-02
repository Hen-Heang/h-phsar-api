# H-Phsar API — Claude Code Instructions

## Working role

- Work as a careful mid-level backend engineer, not an autonomous product owner.
- Understand the current implementation before editing it.
- Prefer the smallest change that fully solves the requested problem.
- Explain the root cause, chosen solution, and important tradeoff briefly.
- Do not silently expand scope, redesign unrelated modules, or modernize dependencies.
- Never claim a test, build, migration, or command succeeded unless it actually ran.
- Never commit, push, rewrite Git history, or discard local work unless explicitly requested.

## Stable project facts

- Java 17, Spring Boot 3.2.5, Maven Wrapper, MyBatis XML, PostgreSQL.
- Keep MyBatis. Do not introduce JPA or Hibernate.
- Main flow: Controller → Service → Repository interface → Mapper XML → PostgreSQL.
- Roles are `SUPPLIER`, `BUYER`, and `ADMIN`. Do not reintroduce Distributor/Retailer terminology.
- Order statuses are `CART`, `DRAFT`, `PENDING`, `PROCESSING`, `DISPATCHED`, `COMPLETED`, `REJECTED`, `CANCELLED`.
- Use `docs/ORDER_WORKFLOW.md` and `OrderStatus.java` as the source of truth for order states and transitions.
- Never persist Java enum ordinals or scatter numeric order-status IDs in Java.
- `README.md` may contain older examples. Verify lifecycle behavior against code and `docs/ORDER_WORKFLOW.md`.
- `DatabaseInitializer` and `schema.sql` are transitional database setup mechanisms. Do not activate or redesign Flyway unless the task explicitly covers migrations.
- For new API code, use `common/api` response classes and current exception handling, not legacy response models.
- Use `docs/DEVELOPER_GUIDE.md` for existing project patterns and `docs/SQL_GUIDE_BEGINNER_TO_ADVANCED.md` only when SQL learning detail is needed.

## Frontend-integration scope

When a task is primarily a UX/UI change, keep it frontend-only.

Change backend files only when a small, verified contract defect blocks an already-supported UI feature. Do not use UI work as a reason to:

- Redesign business workflows
- Change database schema, transaction boundaries, or inventory semantics
- Change authentication architecture
- Add unsupported Admin capabilities
- Standardize every API response in an unrelated broad refactor

Verify controller paths, HTTP methods, request/response DTOs, pagination, and authorization before frontend changes. Configured base paths include `/api/v1/suppliers` and `/api/v1/buyers`. Confirm Admin endpoints from actual controllers before use — do not guess.

## Required task workflow

1. Inspect `git status --short`, `git diff --stat`, and relevant changed files.
2. Restate the requested outcome and define the smallest safe scope.
3. Search only the relevant controller, service, repository, mapper, model, and tests.
4. Present a short plan of 3–6 steps before a multi-file change.
5. Implement one coherent change at a time.
6. Run the narrowest relevant tests first.
7. Run the full test suite or package build only when the change justifies it.
8. Review `git diff --check` and the final diff.
9. Report changed files, tests run, failures, remaining risks, and the next recommended step.

## Architecture boundaries

- Controllers handle HTTP mapping, validation entry, authorization annotations, and response construction.
- Services own business rules, ownership checks, transaction boundaries, and orchestration.
- Repository interfaces define database operations; Mapper XML contains SQL.
- Do not place SQL in controllers or services.
- Do not place business workflow decisions in Mapper XML.
- Multi-write business operations must be atomic at the service layer.
- Use constructor injection; follow existing Lombok and package conventions.
- Reuse current abstractions before creating a new layer.
- Follow current package and naming conventions before inventing a new pattern.
- Use `@Transactional` on public service methods that perform one business operation with multiple writes; do not rely on same-class self-invocation for transactional behavior.
- Do not catch and swallow exceptions that should trigger rollback.
- Use existing domain exceptions and the global exception handler; do not return error strings from services.
- Use Bean Validation for request-shape rules and service validation for database/business rules.
- Use `LocalDate`, `LocalDateTime`, or `Instant`; do not introduce legacy `Date`.
- Use named `OrderStatus` values and the centralized transition service. Do not add magic status IDs.

## API response contract

- Treat the current JSON response shape as a public contract. Do not rename, remove, or change the type of existing fields without explicit approval.
- Use `ApiResponse<T>` for standard successful responses and `PagedResponse<T>` for paginated collections.
- Use `ApiErrorResponse` and the existing global exception handling for failures; do not construct unrelated error shapes in controllers.
- Use `Code` as the centralized application-code and HTTP-status mapping. Reuse an existing value before adding one; give new entries a unique application code, a concise client-safe message, and the correct HTTP status.
- Keep HTTP status and response-body status consistent. Do not return HTTP 200 for a failure merely to preserve an internal code.
- Never expose exception stack traces, SQL details, credentials, tokens, filesystem paths, or internal infrastructure details in response messages.
- Preserve backward compatibility for existing mobile or frontend clients. When a response contract must change, update its tests and documentation in the same task.
- Do not broadly migrate legacy response models during an unrelated feature.

## MyBatis and SQL patterns

- For dynamic sorting, prefer MyBatis `<choose>` with known columns and directions.
- Never build comma-separated ID lists in Java; use MyBatis `<foreach>`.
- New queries should select explicit columns rather than `SELECT *`.
- Use clear table aliases and qualify ambiguous columns.
- Mutation methods should return an affected-row count; services must verify the expected count.
- Use a single guarded `UPDATE` for stock deduction and order-status transitions.
- Target `tb_store_product_detail.id` for inventory; do not deduct by generic `product_id`.
- Use half-open timestamp ranges: `created_date >= #{start}` and `created_date < #{endExclusive}`.
- Use `EXISTS` for existence checks instead of counting every row.
- Preserve `qty >= 0`, order-detail uniqueness, foreign keys, and lifecycle-history integrity.
- Do not write a schema migration without checking existing data and all maintained schema sources.
- Do not run destructive SQL against an unknown database.
- Explain every non-trivial query in simple English when reporting the change.

## Code style

- Optimize first for correct, understandable behavior; concise code is not automatically cleaner code.
- Use names that reflect domain intent. Avoid vague names such as `data`, `info`, `obj`, `temp`, or `process` when a precise term exists.
- Keep methods focused on one coherent responsibility and one level of abstraction.
- Prefer guard clauses when they reduce nesting without hiding required workflow steps.
- Extract duplication only when the repeated code represents the same business concept and is expected to change together.
- Do not create an interface, utility, helper, base class, or pattern for a single speculative use.
- Keep comments for business reasons, constraints, and non-obvious tradeoffs; do not narrate straightforward code.
- Preserve public endpoints, DTO fields, validation, authorization, transaction boundaries, mapper signatures, and observable exception behavior during cleanup.
- Keep refactoring separate from behavior changes whenever practical.
- Do not perform repository-wide renames, formatting sweeps, or import churn during a scoped cleanup.
- Add or update tests around behavior being refactored before changing risky logic.
- Prefer a small sequence of reviewable changes over one large clean-code rewrite.

## Security and data rules

- Never read, print, copy, or commit secrets, tokens, passwords, keystores, or real environment values.
- Obtain the current account from Spring Security or the existing current-user abstraction.
- Never trust buyer, supplier, store, or owner IDs from request input for authorization.
- Enforce ownership in the database query when practical and validate affected-row counts.
- Use `#{}` for every user-controlled MyBatis value.
- Never use `${}` for user-controlled values. Dynamic sorting must use a strict allowlist or MyBatis `<choose>`.
- Stock deduction and status transition updates must remain guarded and concurrency-safe.
- Do not connect tests to production, public, or shared company databases.

## Verification standards

- Pure transition/domain rules: unit tests.
- Mapper, ownership, transaction, and concurrency behavior: PostgreSQL Testcontainers integration tests.
- Do not replace PostgreSQL behavior tests with H2.
- Avoid timing-based sleeps in concurrency tests; use synchronization primitives.
- On test failure, identify the first root cause and inspect only the relevant report or log section.
- Do not weaken a production rule to make a test pass.
- Any backend change must be the smallest compatible contract fix, preserve existing behavior outside the issue, and add/update targeted tests before the full suite.
- Report backend changes separately from frontend changes.
- Use JUnit 5 and current project test conventions. Keep fixtures small, explicit, and deterministic.
- Security regression tests must prove a different buyer or supplier cannot access the resource.
- Transaction tests must prove all earlier writes roll back after a later failure.
- Verify database state after both success and failure.
- Run the targeted test class before the full suite.
- Do not disable, weaken, or delete a failing test without explaining why the expected behavior changed.
- Report the exact command and actual result.

## Documentation standards

- Write clear, simple English suitable for a developer learning the project.
- Keep `docs/ORDER_WORKFLOW.md` synchronized with lifecycle code and SQL.
- Keep SQL examples safe: use `#{}` for values and never teach raw user-input substitution.
- Do not include passwords, tokens, private hosts, database credentials, or copied `.env` values.
- Do not copy stale status-number tables from old README sections.
- Prefer diagrams, tables, and short examples over long repeated prose.
- Mark transitional behavior and known technical debt honestly.
- Link to the source-of-truth file instead of duplicating large sections across documents.

Required checks when backend code changed:

Windows:

```powershell
.\mvnw.cmd test
.\mvnw.cmd verify
```

Unix-like shell:

```bash
./mvnw test
./mvnw verify
```

`test` runs unit tests. `verify` also runs PostgreSQL Testcontainers integration tests named `*IT` and therefore requires Docker.

Always run `git diff --check`.

## Context and token discipline

- Start with targeted `Grep`/`Glob`; do not scan the whole repository by default.
- Do not read `target`, generated output, uploads, logs, or large documentation unless directly relevant.
- Read changed hunks before full files.
- Do not paste complete source files, SQL guides, diffs, or test logs into the response.
- Summarize findings with file paths and line references.
- Use an isolated subagent for noisy code review or long test output.
- Keep plans, progress updates, and final reports concise.
- When the task changes to an unrelated topic, recommend a fresh session instead of carrying stale context.

## Communication format

For implementation tasks, use:

1. Understanding
2. Plan
3. Changes made
4. Verification
5. Risks or follow-up

Teach one important Java, Spring, SQL, or design lesson when it helps the user understand the change.
