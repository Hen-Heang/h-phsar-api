# Concurrency — Java/Spring 구현 예시

> Core 원칙: `docs/checklists/concurrency.md` 참조.
> 이 파일은 Java/Spring 언어 특화 탐지 키워드 및 코드 예시를 제공한다(제네릭, 고유색 없음).

---

## CONC-01 — 외부 변경 연산 멱등성

**탐지 키워드:** `멱등성`, `idempotency`, `거래키 중복 체크`, `중복 요청`, `재시도 안전`, `at-least-once`

**안티패턴:**

```java
// ❌ 외부에서 타임아웃 후 재요청 시 그대로 재처리됨
@PostMapping("/resource/process")
public Response process(@RequestBody ProcessRequest req) {
    resourceService.process(req); // 외부 거래키 중복 체크 없음
    return Response.ok();
}
```

**모범 패턴:**

```java
@PostMapping("/resource/process")
public Response process(@RequestBody ProcessRequest req) {
    // 1. 외부 거래키(또는 idempotency-key) 사전 체크
    if (resourceService.existsByExternalTxId(req.getExternalTxId())) {
        return Response.duplicate(req.getExternalTxId()); // 기존 결과 반환
    }
    // 2. UNIQUE 제약 + INSERT (race 방지)
    resourceService.processWithIdempotency(req);
    return Response.ok();
}
```

**DB 보강:** 외부 거래키 컬럼에 UNIQUE INDEX 설치 → 동시성 race도 차단.

---

## CONC-03 — 외부 노출 변경 API 복구 계약

**탐지 키워드:** `취소 API`, `원거래 조회`, `보상 트랜잭션`, `compensation`

**체크 항목:** 외부 시스템에 변경 API를 신설하면 다음 3종 세트가 함께 제공되어야 한다.

1. **변경 API** — 거래 처리
2. **거래키 기반 원거래 조회 API** — 외부가 타임아웃 시 결과 조회
3. **취소(보상) API** — 외부가 결과 미수신 시 거래 취소

## CONC-02 — 비동기 제출 중복 방어

**탐지 키워드:** `더블클릭`, `로딩바`, `버튼 disabled`, `중복 클릭 방어`, `중복 제출`, `isSubmitting`

**안티패턴:**

```javascript
// ❌ 클릭 직후 같은 버튼이 즉시 다시 눌릴 수 있음
$('#submitBtn').on('click', function () {
    $.ajax({
        url: '/api/submit',
        type: 'POST',
        data: payload,
        success: function (res) { alert('완료'); }
    });
});
```

**모범 패턴 (프로젝트 공통 패턴 활용):**

```javascript
$('#submitBtn').on('click', function () {
    const $btn = $(this);
    if ($btn.prop('disabled')) { return; }
    $btn.prop('disabled', true);
    /* 프로젝트 로딩바 유틸.show() */ // 로딩바
    $.ajax({
        url: '/api/submit',
        type: 'POST',
        data: payload,
        success: function (res) { /* ... */ },
        complete: function () {
            /* 프로젝트 로딩바 유틸.hide() */
            $btn.prop('disabled', false);
        }
    });
});
```

**연계 룰:** 서버 측 멱등성 룰(lang 동시성 체크리스트 CONC-01)과 **반드시 함께** 적용 — UI는 사용자 환경에 따라 우회될 수 있다.
