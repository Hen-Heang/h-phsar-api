---
name: clean-code
description: Review and improve one explicitly scoped H-Phsar Java file, package, or concern while preserving behavior and verifying the refactor.
argument-hint: "[file, package, or focused clean-code concern]"
disable-model-invocation: true
model: sonnet
effort: high
---

Improve clean code only within this scope: $ARGUMENTS

If the scope is missing or repository-wide, stop and ask for a specific file, package, or concern.

1. Inspect `git status --short`, the scoped diff, and relevant tests. Protect all existing work.
2. Define the behavior and contracts that must remain unchanged.
3. Ask the `clean-code-reviewer` agent for a read-only assessment of this scope.
4. Present a short refactoring plan ordered by risk and value.
5. Change no more than three production files unless the user explicitly authorizes a broader scope.
6. Improve names, responsibilities, control flow, duplication, and testability only where the benefit is concrete.
7. Do not change endpoints, response fields, SQL behavior, schema, authorization, transactions, dependencies, or business rules under the label of cleanup.
8. Run the narrowest relevant tests, then `git diff --check`.
9. Review the final diff for accidental behavior changes and unnecessary churn.
10. Do not commit or push.

Report:
- Scope
- Clean-code problems addressed
- Files changed
- Verification commands and actual results
- Behavior intentionally preserved
- Remaining optional improvements
