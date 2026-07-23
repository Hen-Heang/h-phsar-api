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
- Roles are `SUPPLIER` and `BUYER`. Do not reintroduce Distributor/Retailer terminology.
- Use `docs/ORDER_WORKFLOW.md` as the source of truth for order states and transitions.
- Never persist Java enum ordinals or scatter numeric order-status IDs in Java.
- `README.md` may contain older examples. Verify lifecycle behavior against code and `docs/ORDER_WORKFLOW.md`.
- `DatabaseInitializer` and `schema.sql` are transitional database setup mechanisms. Do not activate or redesign Flyway unless the task explicitly covers migrations.
- For new API code, use `common/api` response classes and current exception handling, not legacy response models.
- Use `DEVELOPER_GUIDE.md` for existing project patterns and `SQL_GUIDE_BEGINNER_TO_ADVANCED.md` only when SQL learning detail is needed.

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
