# Calculation — Java/Spring 구현 예시

> Core 원칙: `docs/checklists/calculation.md` 참조.
> 이 파일은 Java/SQL 언어 특화 탐지 키워드 및 코드 예시를 제공한다(제네릭, 고유색 없음).

---

## CALC-01 — 누적 수량 계산 정합성

**탐지 키워드:** `한도`, `잔여 산정`, `누적 사용`, `재고 차감`, `한도 계산`

**안티패턴:**

```java
// ❌ 이미 사용한 수량을 빼지 않음 → 한도 초과 허용
if (totalLimit - requested > 0) { return ALLOW; }
```

**모범 패턴:**

```java
// ✅ 잔여 = 한도 − 누적 사용 − 신규 요청
long remaining = totalLimit - cumulativeUsed - requested;
if (remaining < 0) {
    throw new LimitExceededException(itemId, remaining);
}
```

**테스트 의무:** 잔여 = 0, < 0, 동시 요청 시 race 케이스 JUnit 작성.

---

## CALC-02 — 최종·배치 단계 불변식 재검증

**탐지 키워드:** `배치 재검증`, `잔여 재검증`, `한도 재검증`, `사전 검증만`, `UI 검증 의존`

**안티패턴:** 화면/입력 단계에서 한도를 검증했다는 이유로 배치는 그대로 INSERT.

**모범 패턴:** 배치 진입 직전에 **한도/잔여 재검증 SELECT** 후 부족 시 해당 건 SKIP.

```java
// 배치 진입 직전
long remaining = limitMapper.selectRemainingLimit(itemId);
long requested = sumRequested(targetList);
if (remaining < requested) {
    log.warn("한도 부족 — 일부 건 SKIP. remaining={}, requested={}", remaining, requested);
    targetList = filterWithinLimit(targetList, remaining);
}
```
