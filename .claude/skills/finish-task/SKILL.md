---
name: finish-task
description: Verify a completed H-Phsar task with targeted tests, build checks, and a concise final report.
argument-hint: "[optional targeted test class]"
disable-model-invocation: true
context: fork
agent: test-runner
---

Verify the current task.

1. Inspect changed files.
2. If an argument is provided, run that targeted test: $ARGUMENTS
3. Otherwise infer the narrowest relevant test class.
4. Run `git diff --check`.
5. Run the full test suite only when targeted verification succeeds and the scope warrants it.
6. Do not modify code, commit, or push.
7. Return a concise verification report and smallest next action.
