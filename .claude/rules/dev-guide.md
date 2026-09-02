# 개발 가이드 — 공통 정책

> **적용 대상:** `**/*`

---

## 1. 가이드라인 매핑

> **데이터 위치**: `.claude/config/project.yaml`
> - `projects[].guideline` (프로젝트별 직접 지정 — `{backend, frontend?}`)
> - `conditionalGuides` (조건부, 프로젝트 무관)
>
> 본 파일에는 매핑 표를 두지 않는다. 변경 시 `project.yaml` 만 수정한다.

해석 규칙:

1. 프로젝트 → `project.yaml projects[]` 에서 해당 항목 조회
2. `projects[].guideline.backend` 적용 (필수)
3. ORM/매퍼 도구 작업(쿼리 매퍼 작성·수정) 시, 사용하는 언어팩의 매퍼/쿼리 가이드를 추가로 로드한다.

가이드라인 파일은 자동 로드되지 않으며, 해당 프로젝트 작업 시 필요에 따라 Read 로 참조한다. 작업 대상 프로젝트의 가이드 파일과 **프로젝트 루트의 CLAUDE.md** 를 반드시 확인한다.

프론트엔드 포함 프로젝트는 `projects[].guideline.frontend` 배열을 함께 로드한다.

---

## 2. 공통 유틸 라이브러리 사용 원칙

### 2-1. 기본 원칙

1. 유틸 기능이 필요할 때 **직접 구현 전에 반드시 공통 유틸 라이브러리에서 먼저 찾는다.**
2. 공통 유틸에 필요한 기능이 **없을 경우, 신규 추가 여부를 반드시 사용자에게 확인한 후 진행한다.**
3. 모듈 전용 유틸(`util/` 패키지)은 해당 모듈에서만 사용되는 기능에 한해 허용한다.

### 2-2. 라이브러리 정보

- **아티팩트**: `project.yaml commonUtilsArtifact` 단일 출처. 버전은 각 프로젝트 `pom.xml` 참조.
- **소스 위치**: 외부 라이브러리 (별도 관리)
- **인벤토리**: 공통 유틸 프로젝트 루트의 `CLAUDE.md` 참조.

### 2-3. 모듈 전용 util/ 허용 기준

- **허용**: 해당 모듈에서만 사용되는 특수 로직 (예: 도메인 코드 매핑, 외부 시스템 어댑터)
- **금지**: 공통 유틸 라이브러리에 이미 존재하는 기능의 중복 구현
- **금지**: 여러 모듈에서 공통으로 필요한 유틸을 개별 모듈의 `util/` 에 구현 (→ 공통 유틸 추가 요청)

---

## 2-4. MyBatis 매퍼 가이드

- MyBatis mapper 작성·수정 시 `docs/guideline/guide-mybatis.md`를 참조한다.

---

## 3. 로깅 설정 공통 원칙 (Logback)

- 로그 설정 파일: `logback-spring.xml`
- 로그 레벨은 환경별로 분리한다. (`local` / `dev` / `prod` 프로파일)
- `RollingFileAppender` 사용 시 `maxHistory` 및 `totalSizeCap` 설정은 **서버 cron 이 로그 파일 정리를 별도로 관리하므로 설정하지 않는다.**
- 분산 추적 MDC 키는 `project.yaml tracing.mdcKey` 단일 출처. 모든 로그 항목·비동기 컨텍스트에 전파한다.

---

## 4. {{config.build.tool}} 빌드/테스트

- 표준 {{config.build.tool}} 명령 사용. 패키징·실행은 빌드 도구 표준 명령을 따른다. 모듈 테스트는 `{{config.test.command}}`, 단일 테스트는 `{{config.test.command}} {{config.test.classFilter}}`, 커버리지는 `{{config.coverage.command}}`.
- 프로젝트 고유 빌드 인자 = `project.yaml projects[].buildArgs` 단일 출처. `buildArgsRequired: true` 미지정 시 빌드 차단.
- 멀티모듈 모듈명 = 프로젝트 `CLAUDE.md ## Overview` 서브모듈 표 참조.
- 프로젝트별 `CLAUDE.md ## Build & Run` 에 표준 명령 반복 기재 금지.

---
