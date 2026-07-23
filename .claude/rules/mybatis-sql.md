---
paths:
  - "src/main/resources/mapper/**/*.xml"
  - "src/main/resources/script/**/*.sql"
  - "src/main/java/**/repository/**/*.java"
---

# MyBatis and PostgreSQL Rules

- Bind values with `#{}`. Treat `${}` as unsafe unless it selects a structural SQL fragment from a strict server-side allowlist.
- For dynamic sorting, prefer MyBatis `<choose>` with known columns and directions.
- Never build comma-separated ID lists in Java; use MyBatis `<foreach>`.
- New queries should select explicit columns rather than `SELECT *`.
- Use clear table aliases and qualify ambiguous columns.
- Enforce account/store/order ownership in mutation and sensitive read queries.
- Mutation methods should return an affected-row count; services must verify the expected count.
- Use a single guarded `UPDATE` for stock deduction and order-status transitions.
- Target `tb_store_product_detail.id` for inventory; do not deduct by generic `product_id`.
- Use half-open timestamp ranges: `created_date >= #{start}` and `created_date < #{endExclusive}`.
- Use `EXISTS` for existence checks instead of counting every row.
- Preserve `qty >= 0`, order-detail uniqueness, foreign keys, and lifecycle-history integrity.
- Do not write a schema migration without checking existing data and all maintained schema sources.
- Do not run destructive SQL against an unknown database.
- Explain every non-trivial query in simple English when reporting the change.
