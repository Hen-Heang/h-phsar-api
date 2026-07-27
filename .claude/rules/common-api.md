---
paths:
  - "src/main/java/com/henheang/hphsar/common/api/**/*.java"
  - "src/main/java/com/henheang/hphsar/controller/**/*.java"
  - "src/main/java/com/henheang/hphsar/exception/**/*.java"
---

# Common API Response Rules

- Treat the current JSON response shape as a public contract. Do not rename, remove, or change the type of existing fields without explicit approval.
- Use `ApiResponse<T>` for standard successful responses and `PagedResponse<T>` for paginated collections.
- Use `ApiErrorResponse` and the existing global exception handling for failures; do not construct unrelated error shapes in controllers.
- Use `Code` as the centralized application-code and HTTP-status mapping. Reuse an existing value before adding one.
- Give new `Code` entries a unique application code, a concise client-safe message, and the correct HTTP status.
- Keep HTTP status and response-body status consistent. Do not return HTTP 200 for a failure merely to preserve an internal code.
- Keep controllers focused on request mapping, validation entry, authorization annotations, and response construction. Business decisions remain in services.
- Never expose exception stack traces, SQL details, credentials, tokens, filesystem paths, or internal infrastructure details in response messages.
- Preserve backward compatibility for existing mobile or frontend clients. When a response contract must change, update its tests and documentation in the same task.
- Do not broadly migrate legacy response models during an unrelated feature. Use these classes for new API code and make compatibility changes only within the requested scope.
