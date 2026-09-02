# Data Access — Java/Spring/SQL 구현 예시

> Core 원칙: `docs/checklists/data-access.md` 참조.
> 이 파일은 Java/Spring/PostgreSQL 언어 특화 탐지 키워드 및 코드 예시를 제공한다.

---

## DATA-01 — 신뢰할 수 없는 입력의 쿼리 범위 통제

**탐지 키워드:** `generate_series`, `날짜 채우기`, `시계열 생성`, `날짜 차원`, `CROSS JOIN`, `LATERAL`

**안티패턴 1 — 대범위 정적 호출:**

```sql
-- ❌ 수천만 행 생성 → 메모리·CPU 폭발
SELECT generate_series(1, 100000000);

-- ❌ 100년 일자 채우기 (≈ 36,500 행 × JOIN 카디널리티 폭발)
SELECT * FROM generate_series('1970-01-01'::date, '2070-12-31'::date, '1 day') d
CROSS JOIN TB_LARGE_TABLE t;
```

**안티패턴 2 — 사용자 입력 기반 동적 범위:**

```xml
<!-- ❌ 외부 입력으로 무제한 범위 가능 → DoS 위험 -->
<select id="selectDailyStats" resultType="StatDto">
    SELECT d.day, COUNT(t.id)
    FROM   generate_series(#{startDate}, #{endDate}, '1 day') AS d
    LEFT JOIN TB_TXN t ON t.txn_dt = d.day
    GROUP BY d.day
</select>
```

`endDate - startDate` 가 수년 이상이면 카디널리티 폭발.

**모범 패턴:**

```java
// ✅ 1. 애플리케이션 레이어 사전 검증 (Bean Validation)
public class StatRangeReq {
    @NotNull private LocalDate startDate;
    @NotNull private LocalDate endDate;

    @AssertTrue(message = "조회 범위는 최대 366일")
    public boolean isRangeValid() {
        return ChronoUnit.DAYS.between(startDate, endDate) <= 366;
    }
}
```

```xml
<!-- ✅ 2. SQL 측에서도 범위 상한 보강 (이중 방어) -->
<select id="selectDailyStats" resultType="StatDto">
    SELECT d.day, COUNT(t.id)
    FROM   generate_series(
               #{startDate},
               LEAST(#{endDate}, #{startDate}::date + INTERVAL '366 days'),
               '1 day'
           ) AS d
    LEFT JOIN TB_TXN t ON t.txn_dt = d.day
    GROUP BY d.day
</select>
```

**체크 항목:**
- `generate_series` 의 `start`/`stop` 인자가 **상수 또는 검증된 변수**인가? (사용자 입력 직접 주입 금지)
- 예상 카디널리티가 **합리적 범위(수만 행 이내)** 인가? (일별 생성 시 ≤ 366일, 월별 ≤ 120개월 등 도메인 적정 상한 적용)
- JOIN 시 한쪽이 인덱스 활용 가능한 키로 좁혀지는가? (`generate_series × 대용량 테이블` CROSS JOIN 회피)
- 사용자 입력 기반 범위는 Bean Validation(`@AssertTrue`/`@Max` 등)으로 사전 차단했는가? + SQL 측 `LEAST`/`GREATEST` 로 이중 방어했는가?

**연계 룰:** RES-01 (Long SQL Hot-Path), DATA-02 (DB 슬로우쿼리/통계 도구).

---

## DATA-02 — 느린 쿼리 관측 도구 상시 운영

**탐지 키워드:** `pg_stat_statements`, `log_min_duration_statement`, `autovacuum`, `pg_hint_plan`, `옵티마이저`

**체크 항목:**
- `log_min_duration_statement = 1000` (1초+ 쿼리 자동 로깅)
- `pg_stat_statements` 익스텐션 설치 및 주간 슬로우쿼리 리포트 자동화
- `autovacuum_analyze_scale_factor` 트래픽 패턴에 맞춰 튜닝 (기본 0.2 → 0.01 검토)

**적용 영역:** PostgreSQL 운영 환경 (개발·스테이징·운영 DB 포함).
