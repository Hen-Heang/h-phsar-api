---
paths:
  - "**/*.md"
---

# Documentation Rules

- Write clear, simple English suitable for a developer learning the project.
- Keep `docs/ORDER_WORKFLOW.md` synchronized with lifecycle code and SQL.
- Keep SQL examples safe: use `#{}` for values and never teach raw user-input substitution.
- Do not include passwords, tokens, private hosts, database credentials, or copied `.env` values.
- Do not copy stale status-number tables from old README sections.
- Prefer diagrams, tables, and short examples over long repeated prose.
- Mark transitional behavior and known technical debt honestly.
- Link to the source-of-truth file instead of duplicating large sections across documents.
