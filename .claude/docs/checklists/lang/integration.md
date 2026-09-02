# Integration — Java/Spring 구현 예시

> Core 원칙: `docs/checklists/integration.md` 참조.
> 이 파일은 Java 언어 특화 탐지 키워드 및 코드 예시를 제공한다.

---

## INTG-01 — 생산자 응답 형태 ↔ 소비자 기대 타입 정합

**탐지 키워드:** `ResponseEntity`, `@ResponseBody`, 응답 래퍼, `List<>` vs 래퍼 DTO

**안티패턴:**

```java
// 생산자: 컨트롤러가 래퍼로 감싸 반환
@GetMapping("/orders")
public ApiResult<List<OrderDto>> list() { return ApiResult.ok(orderService.findAll()); }
// 소비자: List<OrderDto> 로 역직렬화 → success/data 봉투 누락·오매핑
```

**모범 패턴:**

```java
// 응답 래퍼 계약을 양쪽이 동일 타입으로 공유
public record ApiResult<T>(boolean success, T data) {}
// 소비자도 ApiResult<List<OrderDto>> 로 역직렬화 후 data() 사용
```

---

## INTG-02 — 직렬화 경계 필드명·옵셔널 일관성

**탐지 키워드:** `@JsonProperty`, `@JsonNaming`, snake_case, `Optional`, `null`

**안티패턴:**

```java
public class ThemeDto {
    private String thumbnailUrl; // ❌ JSON 은 thumbnail_url → 매핑 미지정 시 null
}
```

**모범 패턴:**

```java
public class ThemeDto {
    @JsonProperty("thumbnail_url")
    private String thumbnailUrl; // ✅ 경계 필드명 명시 매핑
}
// 또는 클래스에 @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class) 일괄 적용
```

---

## INTG-03 — 유한 집합(enum) ↔ 분기 완전성

**탐지 키워드:** `enum`, `switch`, `default`, 상태 전이

**안티패턴:**

```java
String label(Status s) {
    switch (s) {
        case PENDING: return "대기";
        case APPROVED: return "승인";
        // ❌ REJECTED 누락 → null 반환 후 NPE
    }
    return null;
}
```

**모범 패턴:**

```java
String label(Status s) {
    return switch (s) {        // ✅ switch 식: 모든 enum 분기 강제
        case PENDING -> "대기";
        case APPROVED -> "승인";
        case REJECTED -> "거절";
    };
}
```

---

## INTG-05 — 동기/비동기 응답 형태 구분

**탐지 키워드:** `CompletableFuture`, `@Async`, 배치 결과, 즉시 반환, `ACCEPTED`

**안티패턴:**

```java
// 즉시 202 로 jobId 만 반환하는데 호출측이 결과 필드 즉시 접근
JobAccepted res = client.startBatch(req); // { jobId }
int failed = res.getFailedCount();         // ❌ 아직 없음
```

**모범 패턴:**

```java
JobAccepted accepted = client.startBatch(req);            // { jobId }
BatchResult result = client.getResult(accepted.jobId());  // ✅ 완료 후 결과 형태 조회
int failed = result.failedCount();
```
