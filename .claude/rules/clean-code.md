---
paths:
  - "src/main/java/**/*.java"
  - "src/test/**/*.java"
---

# Clean Code Rules

- Optimize first for correct, understandable behavior; concise code is not automatically cleaner code.
- Use names that reflect domain intent. Avoid vague new names such as `data`, `info`, `obj`, `temp`, or `process` when a precise term exists.
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
