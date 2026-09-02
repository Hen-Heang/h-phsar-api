# 프론트엔드 리뷰 규칙 (code-review 참조)

> 프로젝트별 JS 패턴 매핑(조직 특화)은 **`{{config.frontend.patternsTable}}`** 단일 출처. 빈 값이면 본 파일의 일반 규칙만 적용 (XSS·== 비교·var 사용 등 언어 레벨 점검).

<Project_JS_Patterns>

**조직 특화 패턴 표**: `{{config.frontend.patternsTable}}` 파일을 Read 하여 프로젝트별 `(JS 패턴, AJAX 방식, UI 컴포넌트)` 표를 로드. 변경 파일의 프로젝트와 대조하여 [W07e] 판정.

**핵심 원칙**: 프로젝트별 기존 패턴 혼합 금지. 신규 코드는 해당 프로젝트 패턴 준수.

`patternsTable` 빈 값이면 [W07e] 매칭 skip.

</Project_JS_Patterns>

<Static_Resource_Paths>

| 가이드 (guideline.backend) | JS/CSS 경로 | 템플릿 경로 |
|-------------|------------|------------|
| WAR (`guide-webmvc.md`) | `{{config.frontend.staticPathsWar}}` | `{{config.frontend.templatesWar}}` |
| JAR (`guide-webfullstack.md`) | `{{config.frontend.staticPathsJar}}` | `{{config.frontend.templatesJar}}` |

</Static_Resource_Paths>

<Frontend_Severity_Mapping>

| 이슈 | severity ID | 설명 |
|------|------------|------|
| XSS (`.html(userInput)`, `innerHTML`) | **C04** | 사용자 입력 HTML 직접 삽입 |
| `th:utext` with user input | **C04** | 이스케이프 안 된 HTML 렌더링 (XSS 취약점) |
| 민감정보 하드코딩 (API Key, password in JS) | **C06** | JS에 민감 정보 노출 |
| AJAX 중복 요청 방지 미적용 | **W02** | `isSubmitting` 플래그 없음 |
| `==` 대신 `===` 미사용 | **W03** | 타입 비교 누락 |
| `var` 사용 (신규 코드) | **W07d** | 컨벤션 위반 |
| 프로젝트 JS 패턴 불일치 | **W07e** | 해당 프로젝트 패턴 미준수 |
| `const` 대신 `let` (재할당 없는 변수) | **S02** | 코드 품질 개선 |
| 코드 구조 (50줄 초과, 중첩 3단계 초과) | **S04** | 가독성 개선 |
| `console.log` 잔재 | **S05** | 디버그 코드 잔재 |

</Frontend_Severity_Mapping>

<Thymeleaf_Rules>

| 규칙 | 판정 |
|------|------|
| POST/PUT/DELETE 요청에 CSRF 토큰 누락 | **C05** (인증/인가 누락) |
| `th:utext` 사용 (사용자 입력 포함) | **C04** (XSS 취약점) — `th:text` 사용 권장 |
| `eval()` / `new Function()` 사용 | **C04** (XSS 취약점) |
| 서버 주입 전역변수가 `_variableName_` 패턴 미준수 | **W07f** (컨벤션 위반) |
| `th:inline="javascript"` 블록에 `/*<![CDATA[*/.../*]]>*/` 미래핑 | **W07f** (컨벤션 위반) |
| inline 이벤트 핸들러 (`onclick` 등) 사용 | **W07e** (패턴 불일치) |

</Thymeleaf_Rules>

<AJAX_Error_Handling>

- 프로젝트 AJAX 래퍼 사용 시 → 에러/500 콜백 존재 확인 (프로젝트 프런트 가이드 정의 기준)
- `$.ajax()` 사용 시 → `error` 콜백 또는 `.fail()` 체이닝 확인
- 오류 처리 미구현 시 → **W05** (예외 삼킴과 동일 맥락)

</AJAX_Error_Handling>
