---
paths:
  - "src/main/java/**/*.java"
---

# Java and Spring Rules

- Follow the current package and naming conventions before inventing a new pattern.
- Keep controllers thin. Do not perform repository calls or business calculations in controllers.
- Put workflow, ownership, validation across entities, and transaction boundaries in services.
- Use `@Transactional` on public service methods that perform one business operation with multiple writes.
- Do not rely on same-class self-invocation for transactional behavior.
- Do not catch and swallow exceptions that should trigger rollback.
- Use existing domain exceptions and the global exception handler; do not return error strings from services.
- Obtain authenticated identity from Spring Security or the current-user provider, never from request ownership fields.
- Use constructor injection and existing Lombok conventions.
- Use Bean Validation for request-shape rules and service validation for database/business rules.
- Use `LocalDate`, `LocalDateTime`, or `Instant`; do not introduce legacy `Date`.
- Use named `OrderStatus` values and the centralized transition service. Do not add magic status IDs.
- Preserve compatibility unless the task explicitly authorizes an API-breaking change.
- Avoid broad renames, formatting-only edits, and unrelated cleanup in a functional change.
