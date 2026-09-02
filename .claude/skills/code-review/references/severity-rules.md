# severity-rules — code-review 패턴 데이터 (L3 결정론 자산)

> **단일 출처**. SKILL.md 가 STEP 1 매칭 시 본 파일을 Read 한다. 조직별 룰 추가/수정은 본 파일만 편집.
>
> severity_rules 매칭 알고리즘은 [`severity-algorithm.md`](severity-algorithm.md) STEP 1 참조.
>
> **변수 치환**: `{{config.commonUtilsArtifact}}` 같은 표기는 `.claude/config/project.yaml` 의 해당 키로 치환된다.
>
> **심각도는 ID 접두가 정한다** — `C**`=Critical / `W**`=Warning / `S**`=Suggestion. 본 파일은 층별 단편(core → 언어팩 → 프레임워크팩)을 이어 붙인 결과이고, 프레임워크 단편은 `## {프레임워크} 특화 룰` 같은 자체 제목 아래에 여러 심각도의 룰을 함께 둔다. **섹션 제목이 아니라 ID 접두로 판정한다** (스캐너·리뷰 에이전트 공통). 심각도 컬럼을 따로 가진 표(`| 코드 | 심각도 | 룰 | 축 |`)는 그 컬럼이 우선이다.

---

## Critical

> 런타임 장애 또는 보안 사고 직결 이슈만 해당. 코드 스타일/컨벤션은 절대 Critical 이 아니다.

| ID | 이름 | keywords | 설명 | 축 | 표준 |
|----|------|----------|------|----|------|
| C01 | NPE 가능성 | null 체크 없이, NullPointerException, NPE | null 체크 없이 메소드 호출 | R |  |
| C02 | ClassCastException 가능성 | 타입 캐스팅, ClassCastException, instanceof 미확인 | 타입 캐스팅 안전성 미확인 | R |  |
| C04 | XSS 취약점 | XSS, 사용자 입력, HTML 삽입, JS 삽입, th:utext, innerHTML | 사용자 입력을 HTML/JS에 직접 삽입. `th:utext` 포함 | S | CWE-79 / OWASP-A03:2021 |
| C05 | 인증/인가 누락 | 인증 누락, 인가 누락, 권한 체크 없이 | 인증/인가 처리 누락 | S | CWE-862 / OWASP-A01:2021 |
| C06 | 민감정보 하드코딩 | 하드코딩, 비밀번호, API Key, 토큰, password | 비밀번호·API Key·토큰 등 민감정보 하드코딩 | S | CWE-798 / OWASP-A07:2021 |
| C07 | 데이터 손실 가능성 | 트랜잭션 미적용, 롤백 불가, 데이터 손실 | 트랜잭션 미적용·롤백 불가로 인한 데이터 손실 | R |  |
| C08 | 무한루프/무한재귀 | 무한루프, 무한재귀, 종료 조건 없음 | 무한루프 또는 무한재귀 가능성 | R |  |
| C09 | 리소스 미해제 | 커넥션 누수, 메모리 누수, 리소스 미해제, Stream 미닫기 | DB·File·Stream 리소스 미해제로 인한 누수 | R |  |
| C11 | Optional.get() without check | Optional.get(), isPresent 없이, orElse 없이, NoSuchElementException | Optional.get() 을 isPresent()/orElse() 없이 호출 → NPE 직결 | R |  |

---

## Warning

> 즉시 장애는 아니지만 유지보수·성능·코드 품질에 부정적 영향을 주는 이슈. 컨벤션 위반은 여기에 해당.

| ID | 이름 | keywords | 설명 | 축 | 표준 |
|----|------|----------|------|----|------|
| W01 | 미사용 import/변수 | 미사용 import, 미사용 변수, unused | 미사용 import 또는 미사용 변수 | U |  |
| W04 | 성능 저하 가능성 | N+1, 불필요한 루프, 성능 저하 | N+1 쿼리·불필요한 루프 등 성능 저하 가능성 | R |  |
| W05 | 예외 삼킴 | 예외 삼킴, catch 블록, 로그 없이 무시, 빈 catch | catch 블록에서 로그 없이 예외 무시 | R |  |
| W06 | 주석 처리된 코드 잔재 | 주석 처리된 코드, commented out, 코드 블록 잔재 | 주석 처리된 코드 블록 잔재 | U |  |
| W07a | 스타 임포트 | import *, 스타 임포트, wildcard import | `import java.util.*` 등 스타 임포트 | U |  |
| W07b | 네이밍 규칙 위반 | 네이밍 위반, lowerCamelCase, UpperCamelCase, 명명 규칙 | 클래스/메소드/변수 네이밍 컨벤션 위반 | U |  |
| W07c | K&R 브레이스 스타일 위반 | K&R, 브레이스 스타일, 중괄호 위치 | K&R 브레이스 스타일 미준수 | U |  |
| W09 | URL/메서드명 불일치 | URL, 메서드명, 의미 불일치 | URL 과 메서드명의 의미 불일치 | U |  |
| W10 | Lombok DTO 어노테이션 불일치 | @Builder, @Setter, InDTO, OutDTO, Lombok, DTO 어노테이션 | DTO 용도별 Lombok 규칙 위반 | U |  |
| W12 | Factory null 반환 | Factory, null 반환, 예외 throw | Factory 패턴에서 null 반환 대신 예외 throw 미사용 | R |  |
| W13 | 공통 유틸 중복 구현 | 공통 유틸, 중복 구현, 유틸 중복 | `{{config.commonUtilsArtifact}}` 에 이미 존재하는 유틸 기능 중복 구현 | U |  |
| W15 | 루프 내 로깅 | 루프 내 log, for문 log.info, while log.debug, 반복 로깅 | 반복문 내부 반복 로그 호출 → 로그 폭증·성능 저하 | R |  |
| W16 | 비동기 컨텍스트 HashMap | HashMap 동시성, ConcurrentHashMap 미사용, 멀티스레드 HashMap | 멀티스레드 환경에서 HashMap 사용 → ConcurrentHashMap 또는 동기화 필요 | R |  |

---

## Suggestion

> 기능에 영향 없는 품질 개선 제안. 문서화·스타일·구조 개선 등.

| ID | 이름 | keywords | 설명 | 축 | 표준 |
|----|------|----------|------|----|------|
| S01 | JavaDoc 미작성 | JavaDoc, 문서화, 주석 미작성 | JavaDoc 내용 오류 또는 미작성 | Tr |  |
| S03 | 파일 끝 개행 없음 | 개행 없음, EOF, newline | 파일 끝 개행 없음 | U |  |
| S04 | 코드 구조 개선 | 구조 개선, 리팩토링, 가독성 | 코드 구조 개선 아이디어 | U |  |

---

## Suggestion

> **과잉설계 공통 룰 (Core 소유).** 언어·프레임워크 무관하게 성립하는 "덜어낼 것" 판정. 스택 고유 과잉설계(특정 프레임워크 관용구)는 해당 팩 단편이 가진다.
>
> **baseline 심각도는 🔵 Suggestion 고정 — 어떤 경우에도 Warning 이상으로 올리지 않는다.** 과잉설계 판정에는 취향이 섞이므로 커밋 게이트(`base-rule` §4)를 잡으면 우회만 늘어난다. 덜어내기는 제안으로 남기고 차단은 결함에만 쓴다.

| ID | 이름 | keywords | 설명 | 축 | 표준 |
|----|------|----------|------|----|------|
| S90 | 표준 라이브러리 재구현 | 직접 구현, 손으로 구현, 수동 파싱, 재구현, 표준 라이브러리 | 언어 표준 라이브러리·런타임이 이미 제공하는 기능을 직접 구현 | U |  |
| S91 | 구현체 1개 추상화 | 인터페이스 1개, 구현체 하나, 팩토리, 래퍼 클래스, 추상화 계층 | 구현체 또는 호출자가 하나뿐인 인터페이스·팩토리·래퍼·계층 | U |  |
| S92 | 미사용 유연성 | 확장 포인트 미사용, 설정값 미사용, 죽은 분기, 도달 불가 코드 | 아무도 설정하지 않는 옵션, 아무도 타지 않는 분기·확장 포인트 | U |  |
| S93 | 불필요 의존 추가 | 의존 추가, 라이브러리 추가, 신규 의존 | 몇 줄로 되는 일에 새 외부 의존을 추가 | U |  |

> **오탐 방지 — 아래는 과잉설계가 아니다. 지적하지 않는다.**
> 요구사항·계약이 명시적으로 요구한 추상화, 신뢰 경계의 입력 검증, 데이터 손실을 막는 예외 처리, 보안 조치, 접근성 처리, 그리고 최소 단위 테스트 1개.

## jQuery/프론트 특화 룰 (fw-jquery)

| ID | 이름 | keywords | 설명 | 축 |
|----|------|----------|------|----|
| W02 | AJAX 중복 요청 방지 미적용 | AJAX, 중복 요청, isSubmitting | AJAX 중복 요청 방지 미적용 | R |
| W03 | == 대신 === 미사용 | ==, ===, 타입 비교 | JavaScript 에서 == 대신 === 미사용 | R |
| W07d | var 사용 (JS 신규 코드) | var 사용, var 선언 | JavaScript 신규 코드에서 var 사용 (let/const 권장) | U |
| W07e | 프로젝트 JS 패턴 불일치 | JS 패턴 불일치, AJAX 방식 불일치, 코딩 패턴 불일치 | 해당 프로젝트의 기존 JS 패턴 미준수 | U |
| S02 | const 미사용 | const, let, 재할당 없는 | 재할당 없는 변수에 const 대신 let 사용 | U |
| S05 | console.log 잔재 | console.log, 디버그 로그 | console.log 잔재 | U |

## MyBatis 특화 룰 (fw-mybatis)

| ID | 이름 | keywords | 설명 | 축 |
|----|------|----------|------|----|
| C03 | SQL Injection | SQL Injection, `${}`, MyBatis ${} | MyBatis `${}` 사용. ORDER BY/GROUP BY 컨텍스트는 Warning 하향 ([`severity-algorithm.md`](severity-algorithm.md) 참조) | S |
| S06 | Mapper 메소드명 패턴 위반 | Mapper, 메소드명, CRUD, 네이밍 패턴 | Mapper 메소드명이 `{table}{CRUD}{###}` 패턴 미준수 | U |

## Spring 특화 룰 (fw-spring)

| ID | 이름 | keywords | 설명 | 축 |
|----|------|----------|------|----|
| C10 | @Transactional private/self-invocation | @Transactional private, self-invocation, 같은 클래스 내부 호출, 프록시 우회 | Spring 프록시 무시 → 트랜잭션 미적용 → 데이터 손실 가능 | R |
| W07f | Thymeleaf 전역 변수 패턴 | Thymeleaf 전역 변수, `_variableName_`, 전역변수 패턴 | 서버 주입 전역변수가 `_variableName_` 패턴 미준수 | U |
| W08 | @Validated 검증 미적용 | @Validated, 검증 어노테이션, @Valid | @Validated 선언 후 검증 어노테이션 미적용 | S |
| W11 | MDC x-track-uuid 전파 누락 | MDC, x-track-uuid, ExecutorService, CompletableFuture, @Async, new Thread | 비동기 컨텍스트에서 MDC x-track-uuid 전파 누락 | Tr |
| W14 | @Async void 예외 삼킴 | @Async void, AsyncUncaughtExceptionHandler, 비동기 예외 | @Async void 메소드에서 예외 유실 | R |
