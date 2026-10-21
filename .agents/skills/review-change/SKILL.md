---
name: review-change
description: Review the current H-Phsar working-tree diff for correctness, security, SQL safety, transactions, concurrency, and missing tests.
disable-model-invocation: true
context: fork
agent: backend-reviewer
---

Review the current uncommitted H-Phsar changes.

Use `git diff --stat` first, then inspect only changed hunks and required surrounding code. Follow the backend-reviewer output format. Do not edit files, commit, or push.
