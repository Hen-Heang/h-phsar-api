# Resource — Java 구현 예시

> Core 원칙: `docs/checklists/resource.md` 참조.
> 이 파일은 Java 언어 보편의 리소스 관련 탐지 키워드 및 코드 예시를 제공한다.
> (Spring 트랜잭션·커넥션풀 특화는 fw-spring 의 resource.md.fwpart 가산.)

---

## RES-01 — 동기 핫 경로에 느린 작업 금지

**탐지 키워드:** `한도 체크`, `잔액 체크`, `누적 합계`, `대용량 스캔`, `인덱스 미적용`, `Long SQL`, `N+1`

**안티패턴 예시 (의사 SQL):**

```sql
-- ❌ 매 요청마다 전체 이력을 SUM (인덱스 부재 + 풀스캔)
SELECT SUM(amount)
FROM   transaction_history
WHERE  product_id = ?
  AND  status = 'DONE'
```

**모범 패턴:**
- 누적 합계 컬럼을 별도 집계 테이블에 캐싱하고 INSERT/UPDATE 시 갱신
- `(product_id, status)` 복합 인덱스 + 파티션 검토
- SQL EXPLAIN 결과를 PR에 첨부 (대용량 테이블 접근 시 의무화)

**연계 룰:** DATA-02(슬로우쿼리 도구), CONC-01(외부 재시도 멱등성).

---

## RES-03 — 비동기 누적 작업 상한·만료 처리

**탐지 키워드:** `재처리`, `상태 동기화`, `발송 대기`, `발송 중`, `누적 처리`, `메모리 누수`

**안티패턴:** 실패 건이 무한 누적되는 큐/상태 테이블, 한도/TTL/배치 정리 없음.

**모범 패턴:**
- 재처리 시도 횟수 상한(`retry_count`) + DLQ(데드레터 큐)
- 일정 기간 경과 건 별도 적재 → 메인 큐 정리
- 누적량 임계치 알람(예: 대기 N건 초과)

**적용 영역:** 비동기 워커, 메시지 컨슈머, 알림 데몬, 재처리 큐.

---

## RES-04 — 리소스 사이즈 근거 기반 산정

**탐지 키워드:** `Xmx`, `Xms`, `메모리`, `커넥션풀`, `풀 사이즈`, `리소스 산정`

**체크 항목:**
- JVM 메모리는 **예상 최대 트래픽·누적 객체 수**를 근거로 산정되었는가? (디폴트값 그대로 사용 금지)
- 신규 데몬/배치 추가 시 부하 시나리오 테스트(메모리·GC 추이)가 수행되었는가?
- 커넥션 풀 최대치는 DB 최대 커넥션 / 인스턴스 수 분배가 명시되었는가?

---

## RES-02 — 희소 자원을 외부 호출에 묶지 않기 (Spring 전용)

**탐지 키워드:** `@Transactional`, `TransactionTemplate`, `RestTemplate`, `WebClient`, `HttpClient`, `OkHttp`, `FeignClient`, `커넥션풀`, `HikariCP`, `connection timeout`

**안티패턴:**

```java
// ❌ 트랜잭션 내부에서 외부 API 호출 — 응답 지연 시 커넥션 장기 점유
@Transactional
public void payAndNotify(PayRequest req) {
    paymentMapper.insert(req);              // DB 작업 1
    notifyClient.callExternal(req);         // 외부 API — 평시 100ms, 지연 시 5~30초
    paymentMapper.updateStatus(req);        // DB 작업 2
}
```

**장애 시나리오:**
1. 외부 API 평시 100ms → 일시 지연으로 5~30초 응답
2. `@Transactional` 이 커밋 안 되어 DB 커넥션 1개를 5~30초간 점유
3. 동시 요청 누적 시 HikariCP `maxPoolSize`(예: 20) 단시간 고갈
4. 신규 요청은 `connection is not available` / `acquire timeout` 발생 → 서비스 전체 다운

**모범 패턴 1 — 트랜잭션 분리 (외부 호출 외부화):**

```java
// ✅ 트랜잭션은 DB 작업 전용, 외부 호출은 트랜잭션 밖에서
public void payAndNotify(PayRequest req) {
    paymentService.savePayment(req);              // @Transactional — DB 작업만
    notifyClient.callExternal(req);               // 트랜잭션 밖 외부 호출
    paymentService.markNotified(req.getId());     // @Transactional — DB 작업만
}

@Service
public class PaymentService {
    @Transactional
    public void savePayment(PayRequest req) { paymentMapper.insert(req); }

    @Transactional
    public void markNotified(Long id) { paymentMapper.updateStatus(id); }
}
```

**모범 패턴 2 — 비동기 이벤트 분리 (트랜잭션 커밋 후 발사):**

```java
// ✅ 외부 호출은 Kafka 또는 @TransactionalEventListener(AFTER_COMMIT) 으로 비동기 발사
@Transactional
public void payAndPublishEvent(PayRequest req) {
    paymentMapper.insert(req);
    eventPublisher.publish(new PayCompletedEvent(req));
}

@Async
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void onPayCompleted(PayCompletedEvent e) {
    notifyClient.callExternal(e.getRequest());   // 커밋 후 + 트랜잭션 밖
}
```

**체크 항목:**
- `@Transactional` / `TransactionTemplate.execute(...)` 내부 본문에 `RestTemplate` / `WebClient` / `HttpClient` / `OkHttp` / `Feign` 호출이 없는가?
- 트랜잭션 구간이 *DB 작업 전용*으로 한정되었는가? (외부 호출은 트랜잭션 밖 또는 `AFTER_COMMIT` 비동기)
- HikariCP `maxPoolSize` / `connectionTimeout` 임계치 알람이 모니터링에 등록되었는가?
- 외부 API 호출에 합리적 `connectTimeout` / `readTimeout` 이 설정되었는가? (무제한 대기 금지)

**연계 룰:** OPS-02 (실패 알람 미정의), DATA-02 (DB 슬로우쿼리/통계 도구), RES-04 (JVM/리소스 사이즈 산정).
