---

## 유형별 추가 섹션 (fw-jquery · 프론트)

> **가산 레이어.** jQuery/IIFE 레거시 프론트. 백엔드 언어와 직교하므로 어떤 lang/백엔드 위에도 부착된다.
> 위 백엔드 팩(예: fw-spring)이 web-fullstack Overview 서버·뷰 행을 제공하면, 아래 프론트 행·섹션이 이어서 덧붙는다.

### [web-fullstack] Overview 추가 행 (프론트)

```markdown
| JS      | jQuery x.x.x             |
```

`## 패키지 구조` 뒤, `## Project-Specific Notes` 앞에 추가:

```markdown
## 정적 리소스 디렉토리 구조

표준 트리는 `guide-frontend/common.md` §2 (WAR / JAR 프로젝트별) 단일 출처를 따른다. 본 섹션에는 **프로젝트 고유 차이** (브랜드별 디렉토리, 보안 키패드 리소스 위치, 도메인별 JS 분류, 이례적 경로 등) 만 기재하며 표준 트리를 반복 그리지 않는다.

---

## JavaScript 코딩 패턴

{한 줄 패턴 식별 — 예: "전역 함수 + ajax()" / "jQuery 플러그인 + ajax.send()" / "객체 리터럴 + modal" / "IIFE + UI 네임스페이스"}. 코드 예시·AJAX 호출 방식·오류 처리 패턴은 `guide-frontend/{mvc-pattern|iife-pattern}.md` §4-N 을 따른다.

### {프로젝트 고유 항목} (있을 때만)

- 예외 처리 구조(클래스명·HTTP 상태 매핑) — admin-web 의 `AdminException`/`JsonException`
- 보안 키패드 / 인증 모듈 — 각 프로젝트별 사용 인증 모듈 (예: RSA 키패드, OTP 인증 등)
- UI 컴포넌트 별칭만 (인벤토리 자체는 `guide-frontend/common.md` §9-2 단일 출처) — 예: app-web 의 `modal` 별칭

> UI 컴포넌트 인벤토리(`UI.modal`/`UI.tab`/`UI.page`/`UI.datepicker`/`UI.select`) 는 가이드 §9-2 에 정의돼 있으므로 본 섹션에 표를 반복 그리지 않는다. 프로젝트별 별칭(예: app-web `modal`) 만 짧게 명시한다.
```
# 유형별 CLAUDE.md 템플릿 (fw-spring · 백엔드)

> **가산 레이어.** `update-claude-md` 스킬 Core 는 언어중립 공통 골격(`templates/templates.md`)만 제공한다.
> 이 파일은 fw-spring 팩이 얹는 **Spring 백엔드 유형별 추가 섹션 + 유형 분류표**다.
> 여러 프레임워크 팩이 각자의 `.fwpart` 로 이 파일(`skills/update-claude-md/templates/type-templates.md`)에 덧붙는다 —
> 예: 프론트(jQuery/정적리소스/JS 패턴) 섹션은 **fw-jquery** 팩이 이어서 덧붙인다.
> 스킬 워크플로우는 이 파일이 조립돼 있으면 유형별 섹션·분류를 여기서 읽고, 없으면 공통 골격만 적용한다.

---

## 유형별 추가 섹션 (Spring 백엔드)

### [web-fullstack] Overview 추가 행 (서버·뷰)

대상: `guideline.backend` ∈ 풀스택 웹 가이드 파일 (예: `guide-webfullstack.md`)

```markdown
| 뷰 엔진 | Thymeleaf x.x.x         |
| 패키징  | WAR                     |
```

> 프론트 JS 행(`| JS | jQuery … |`)과 「정적 리소스 디렉토리 구조」·「JavaScript 코딩 패턴」 섹션은 **fw-jquery** 팩이 이어서 덧붙인다(프론트 라이브러리는 백엔드 언어와 직교).

---

### [Library] 제목 아래 아티팩트, 의존성 섹션 변경

대상: `guideline.backend == guide-library.md` (예시: `app-core`, `app-common`)

제목 바로 아래:
```markdown
**아티팩트:** `{{config.baseNamespacePattern}}:{artifactId}:{version}`
```

`## 주요 의존성` → `## 허용 의존성`:
```markdown
## 허용 의존성

| 의존성 | 버전 | 용도 |
|--------|------|------|
| ...    |      |      |

**허용 범위 외 프레임워크 의존성 추가 금지.**
```

`## Project-Specific Notes` 는 가이드(`guide-library.md` Part 1/2) 에 설계 원칙·퍼사드 사용·JDBC 리소스 관리·신규 추가 절차 가 모두 정의되어 있으므로, **본문에 풍부한 도메인 정보(거래 코드 표·9단계 흐름·핵심 테이블·유틸 패키지 인벤토리)** 가 있다면 한 줄 안내만 남긴다:

```markdown
## Project-Specific Notes

표준 패턴(설계 원칙·퍼사드 사용·JDBC 리소스 관리·신규 추가 절차)은 모두 `Development Guide` 의 `guide-library.md` 를 따른다. 본 프로젝트 고유 항목은 위 「업무 설명」/「제공되는 유틸 패키지」 인벤토리에 정리되어 있다.
```

---

### [Batch] 패키지 구조 아래 Job 카테고리 테이블

대상: `guideline.backend == guide-batch.md` (예시: `app-batch`)

```markdown
### {카테고리} ({n}개) — {설명}

| Job Name | 설명 |
|----------|------|
| `{jobName}` | {설명} |
```

`## Build & Run` 은 Batch 의 Job 단위 빌드 구조(빌드 설정 파일 `<finalName>{projectName}-${batch.build.job}</finalName>` + 컴파일러 include 분기) 에 맞춰 다음 형식을 따른다:

```markdown
## Build & Run

```bash
# 단일 Job 빌드 (필수: 인자 없으면 jobs 코드가 컴파일 대상에서 제외됨)
빌드 명령 -Dbatch.build.job={jobName}
# 산출물: target/{projectName}-{jobName}.jar

# 실행
java -jar target/{projectName}-{jobName}.jar

# 테스트 + 커버리지
빌드 명령 verify -Dbatch.build.job={jobName}
```

Job 별로 별도 jar 가 만들어지는 구조다 — 빌드 설정 `<profiles>` 섹션이 Job 각각에 대응한다.
```

---

## 유형 분류 매핑 (템플릿 선택용)

> **프로젝트 목록·분류의 단일 출처는 `.claude/config/project.yaml projects[]` 다.**
> 개별 프로젝트의 분류는 `project.yaml` `projects[].guideline.backend` 가이드 파일명에서 읽는다. 해당 프로젝트 `CLAUDE.md` `## Overview` 의 한국어 라벨은 보조 표시용.

### guideline.backend → 분류 코드

`project.yaml` `projects[].guideline.backend` 가이드 파일명을 유형 분류 코드로 변환한다. Core SKILL 워크플로우 2단계가 (fw-spring 설치 시) 본 표를 참조한다. 아래는 fw-spring 이 소유한 백엔드 가이드에 대한 매핑이며, 다른 프레임워크 팩은 자기 가이드에 대한 매핑을 각자 덧붙인다.

| guideline.backend | 분류 코드 |
|-----------------------|----------|
| `guide-api.md` | web-api |
| `guide-webmvc.md` | web-fullstack |
| `guide-webfullstack.md` | web-fullstack |
| `guide-batch.md` | batch |
| `guide-daemon.md` | daemon |
| `guide-proxy.md` | web-api |
| `guide-library.md` | library |
| `guide-kafka.md` | daemon |
| `guide-redis.md` | daemon |
| `guide-single.md` | web-api |
| (guideline 미지정) | meta |
