---
name: implement-feature
description: Implement one scoped H-Phsar backend feature or bug fix using the project architecture and verification workflow.
argument-hint: "[feature or bug description]"
disable-model-invocation: true
model: sonnet
effort: medium
---

Implement this task: $ARGUMENTS

Rules:
1. Inspect the working tree and protect existing changes.
2. Read only the directly relevant controller, service, repository, mapper, model, tests, and source-of-truth document.
3. State the outcome and a 3–6 step plan.
4. Implement the smallest coherent change.
5. Preserve MyBatis, PostgreSQL, current endpoint compatibility, ownership, transaction, inventory, and lifecycle rules.
6. Add or update the narrowest meaningful tests.
7. Run targeted verification, then `git diff --check`.
8. Do not commit or push.
9. End with changed files, commands/results, risks, and one learning point.

Stop rather than expanding into unrelated cleanup.
