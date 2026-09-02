<!--
═══════════════════════════════════════════════════════════════════════════
 개발 가이드 작성 템플릿  (template.md)
═══════════════════════════════════════════════════════════════════════════
 본 파일을 복사해 `.claude/docs/guideline/guide-{분류}.md` 로 저장한 뒤 채운다.
 project.yaml `projects[].guideline.backend` 에 파일명을 등록해야 실제 로드된다.
 (본 템플릿 자체는 어디서도 참조하지 않으므로 가이드로 로드되지 않는다.)

 ※ 프로젝트 분류 단일 출처 = guideline.backend 가이드 파일명. 별도 type 필드 없음.
   guide 파일 1개 = 프로젝트 분류 1개. 새 분류가 필요하면 guide 파일을 새로 만든다.

 ── 작성 규칙 ──────────────────────────────────────────────────────────────
 1. 한글 섹션 헤더. 최상위 섹션 사이는 `---` 로 구분.
 2. 섹션 순서는 아래 골격을 따른다 (선택 섹션은 해당 없으면 통째로 삭제).
 3. 표 = 명명 규칙, 코드블록 = 패키지 트리·Java 예시, ✅/❌ = 권장/금지 대비.
 4. Java 예시의 클래스·메소드에는 JavaDoc 작성 (base-rule §6).
 5. 환경/프로젝트 값은 `{{config.*}}` 변수로 쓴다 (단일 출처 = project.yaml).
    예: `{{config.baseNamespacePattern}}`, `{{config.db.schema}}`, `{{config.tracing.mdcKey}}`
 6. `{...}` 는 채울 자리. `(예시)` 표시 행은 실제 예시로 교체하거나 삭제.

 ── 금지 ───────────────────────────────────────────────────────────────────
 · `## 체크리스트` 섹션을 만들지 않는다.
   규칙 원본은 본문 §섹션(단일 출처), 리뷰 항목은 아래 `<type-eval>` 펜스가 담당.
   둘을 따로 두면 같은 규칙이 2~3곳에 중복되어 drift 발생.
 · 펜스 내용을 본문에서 복붙하지 말고, 본문 규칙의 "리뷰 관점 키워드"만 추린다.

 ── 작성 후 이 주석 블록은 삭제한다 ──────────────────────────────────────────
-->

# 개발 가이드 — {분류 표시명}

<!-- 적용 대상 = 본 가이드가 적용되는 프로젝트 분류 설명. 유형 = 한 줄 스택 설명. -->
> **적용 대상:** {대상 프로젝트 분류 — 예: REST API 마이크로서비스 / Web MVC(WAR) / Batch}
> **유형:** {Spring Boot 3.x / WAR / Batch ... 한 줄 설명}
> **단일 출처:** `.claude/config/project.yaml` `projects[]` (guideline.backend = `guide-{분류}.md`)
>
> <!-- (선택) 워크스페이스 고유 어노테이션·규약이 있으면만 기재. 없으면 이 줄 삭제. -->
> 본 가이드는 워크스페이스 고유 어노테이션을 사용한다: `@{Annotation}` — {용도}

---

## 1. 패키지 구조

<!-- base package + 디렉토리 트리. 각 디렉토리 역할을 `←` 주석으로 한 줄씩. -->
기본 패키지는 `{{config.baseNamespacePattern}}.{영역}` 이며, 아래 구조를 따른다.

```
{{config.baseNamespacePattern}}.{영역}
  ├── controller/      ← {역할}
  ├── service/
  │   └── impl/        ← {역할}
  ├── mapper/          ← {역할}
  ├── model/           ← {역할}
  ├── constants/       ← {역할}
  └── util/            ← {역할}
```

---

## 2. 클래스 명명 규칙

<!-- 레이어/패턴/예시 3열 표. 이 분류에 없는 레이어 행은 삭제. -->
| 레이어 | 패턴 | 예시 |
| ------ | ---- | ---- |
| Controller | `{Domain}Controller` | `{예시}Controller` |
| Service 인터페이스 | `{Domain}Service` | `{예시}Service` |
| Service 구현체 | `{Domain}ServiceImpl` | `{예시}ServiceImpl` |
| Mapper | `{Domain}Mapper` | `{예시}Mapper` |
| 요청 DTO | `{...}` | `{예시}` |
| 응답 DTO | `{...}` | `{예시}` |

---

## 3. DTO / CRUD 명명 규칙

<!-- Mapper 메소드명 패턴 + DTO 클래스명 패턴. 분류에 맞게 기술.
     {table}{CRUD}{###} 계열이면 그 규칙을, select/insert..Action 계열이면 그 규칙을. -->
{Mapper 메소드명·DTO 클래스명 패턴 서술}

```
{패턴 예: {tableNameCamelCase}{C|R|U|D}{###}(ParamType) → ReturnType}
{예시 1}
{예시 2}
```

---

## {N}. {패턴 섹션 제목}

<!-- 이 분류의 핵심 패턴을 섹션 단위로. 예:
     - Factory 패턴 (버전/기관 분기)
     - 응답 템플릿 (ResponseTemplate)
     - 서비스 메소드 명명
     - Lombok 어노테이션 규칙 (DTO 유형별 표)
     - 도메인 코딩 규칙 (Producer/Consumer, POI, Redis TTL 등)
     필요한 만큼 ## 섹션을 추가하고 번호를 잇는다. -->
{설명}

```java
/**
 * {클래스/메소드 설명}.
 *
 * @author {작성자}
 * @since {YYYY.MM.DD}
 */
{Java 예시}
```

<!-- 권장/금지 대비가 필요하면 ✅/❌ 로. -->
```java
// ✅ 허용 — {이유}
{권장 코드}

// ❌ 금지 — {이유}
{금지 코드}
```

---

## {N+1}. 주의사항

<!-- (선택) 이 분류에서 특히 틀리기 쉬운 점·예외·레거시 금지. 없으면 섹션 삭제. -->
- {주의 항목}

---

## {N+2}. 프론트엔드 개발 규칙

<!-- (선택, 프론트엔드 포함 프로젝트만 — guide-webmvc / guide-webfullstack 등. 백엔드 전용이면 삭제.)
     상세 규칙은 guide-frontend/* 단일 출처를 참조만 하고 본문에 복제하지 않는다. -->
프론트엔드(JavaScript, CSS, Thymeleaf) 개발 시 별도 프론트엔드 가이드를 참조한다.

- **가이드 위치**: [guide-frontend/common.md](guide-frontend/common.md) + [guide-frontend/{패턴}.md](guide-frontend/{패턴}.md)
- **정적 리소스 경로**: `{경로}`
- **템플릿 경로**: `{경로}`

---

## 코드리뷰 평가 (code-review)

<!-- ★ 필수 섹션 ★  code-review·mr-review 가 이 펜스 안 항목만 기계 추출해 가이드별 평가로 적용.
     펜스 작성 규칙:
       · `<type-eval>` ~ `</type-eval>` 사이, 안쪽에 빈 줄 1개씩 (md 리스트 렌더 보존).
       · self-contained — "위 §N 참조" 식 금지. 추출기는 펜스 텍스트만 본다.
       · terse 키워드 1줄/항목. 본문 규칙의 "리뷰 관점"만 추린다.
       · severity 코드 [W..]/[S..] 는 선택. 단일 출처 = code-review/references/severity-rules.md.
       · 속성 금지. 가이드 식별 단일 출처는 project.yaml `guideline.backend` 파일명. -->
`/code-review`·`/mr-review` 가 본 가이드 대상 프로젝트의 변경을 리뷰할 때 적용하는 평가 항목.

<type-eval>

- {리뷰 키워드 1} [{선택: W.. / S..}]
- {리뷰 키워드 2}
- {리뷰 키워드 3}

</type-eval>
