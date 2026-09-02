# XML Mapper 쿼리 변경 분석

> code-review 스킬의 Step 3.5에서 참조하는 XML Mapper 분석 절차.

<Detection>

변경된 파일 목록에서 `**/mapper/**/*.xml` 패턴 파일을 추출한다.

> **탐지 범위:** mapper 하위 서브디렉터리(`mapper/common/`, `mapper/bizCard/` 등)와 비표준 경로(`src/main/java/**/mapper/`)도 포함한다. 재귀 패턴 `**/mapper/**/*.xml` 권장.

- 해당 XML이 **없으면** → 이 스텝 건너뜀
- 해당 XML이 **있으면** → 아래 분석 수행

</Detection>

<Query_ID_Extraction>

변경 쿼리 ID를 두 가지 방식으로 병행 추출한다:

1. **직접 추출** — git diff 헝크에서 추가/수정된 라인 중 `id="..."` 패턴을 Grep하여 새로 추가된 쿼리 ID 추출
2. **역추적 추출** — diff 헝크가 기존 쿼리의 내부(동적 SQL `<if>`, `<choose>`, `<foreach>`, WHERE 절 등)만 수정한 경우 → 해당 헝크의 상위 `<select>`, `<insert>`, `<update>`, `<delete>` 태그의 `id` 속성을 XML 파일에서 역추적하여 쿼리 ID를 결정

diff에 쿼리 ID 행이 없더라도 동적 SQL 내부 변경은 인덱스 활용이나 보안에 영향을 줄 수 있어 반드시 탐지해야 한다.

변경된 쿼리 ID가 없으면 → 이 스텝 건너뜀

</Query_ID_Extraction>

<Index_Analysis>

각 변경된 쿼리에 대해:

1. XML 파일에서 해당 쿼리 SQL 전문 읽기
2. SQL에서 테이블명 추출 (FROM, JOIN, INSERT INTO, UPDATE 절)
3. `{{config.db.vendor}}` MCP 도구(예: postgres → `mcp__postgres__query`)로 관련 테이블 인덱스를 **배치 조회** (단일 쿼리로 통합). vendor 별 시스템 카탈로그 사용:

```sql
-- postgres
SELECT tablename, indexname, indexdef FROM pg_indexes
WHERE schemaname = '{{config.db.schema}}' AND tablename IN ('{테이블1}', '{테이블2}', ...);

-- mysql
SELECT TABLE_NAME, INDEX_NAME, COLUMN_NAME, SEQ_IN_INDEX
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = '{{config.db.schema}}' AND TABLE_NAME IN (...);

-- oracle
SELECT TABLE_NAME, INDEX_NAME, COLUMN_NAME, COLUMN_POSITION
FROM ALL_IND_COLUMNS
WHERE TABLE_OWNER = '{{config.db.schema}}' AND TABLE_NAME IN (...);
```

> 테이블별 개별 호출 대신 IN 절로 배치 조회하여 MCP 호출 횟수를 최소화한다.
> MCP 도구가 없으면 인덱스 분석 skip 하고 SQL 구조 점검만 수행.

</Index_Analysis>

<Optimization_Criteria>

분석 항목은 `{{config.customDocs.mybatisGuide}}` 단일 출처를 따른다 (빈 값이면 일반 인덱스 분석만 수행). 본 스킬에 중복 정의 없음:
- **§4 작성 시 자가 점검 8개 항목** — 쿼리 목적·입출력·JOIN 구조·WHERE·동적 SQL·사용자 정의 함수·인덱스 활용·최적화 포인트
- **§5 동적 SQL 분기별 인덱스 활용** — `<if>`·`<choose>`·`<foreach>`·`<where>` 분기별 분석
- **§6 사용자 정의 함수 호출 시 고려 사항** — 호출 위치·빈도·내부 I/O·캐싱
- **§7 인덱스 활용 패턴** — Eq / Range / Seq Scan 판정

복합 PK 부분 활용·드라이빙 테이블 선택은 §4 *최적화 포인트* + §7 항목으로 평가한다.

`customDocs.mybatisGuide` 가 빈 값이면 일반 점검 (인덱스 매칭 / 풀스캔 위험 / N+1 / 동적 SQL 누락 WHERE) 만 수행.

수집한 분석 결과는 리뷰 출력의 `### 🗄️ 쿼리 변경 검증` 섹션에 통합된다. 별도 파일로 저장하지 않는다.

</Optimization_Criteria>
