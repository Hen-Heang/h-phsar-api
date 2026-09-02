# 개발 가이드 — Spring Boot Web

> **적용 대상:** Spring Boot Web(JAR) 프로젝트
> **유형:** Spring Boot 3.x Web (JAR 배포, 내장 Tomcat)
> **단일 출처:** `.claude/config/project.yaml` `projects[]`

---

## 1. 패키지 구조

기본 패키지는 `{{config.baseNamespacePattern}}.{project}` 이며, 아래 구조를 따른다.

```
{{config.baseNamespacePattern}}.{project}
  ├── {Project}Application.java
  ├── config/          ← Spring, Security 설정
  ├── constants/       ← ResultCode, Constants (모듈 자체 정의)
  ├── controller/      ← @RestController 또는 @Controller (도메인별 서브패키지)
  ├── exception/       ← 커스텀 예외
  ├── filter/          ← 서블릿 필터
  ├── handler/         ← 전역 예외 핸들러 (@ControllerAdvice)
  ├── mapper/          ← MyBatis @Mapper (DB 연동 시)
  ├── model/
  │   ├── request/     ← 요청 DTO
  │   └── response/    ← 응답 DTO
  ├── service/         ← Service 인터페이스 + impl/
  └── util/            ← 모듈 전용 유틸 (필요 시)
```

---

## 2. 클래스 명명 규칙

| 레이어 | 패턴 | 예시 |
|--------|------|------|
| Controller | `{Domain}Controller` | `PaymentController`, `PortalController` |
| Service 인터페이스 | `{Domain}Service` | `PaymentService` |
| Service 구현체 | `{Domain}ServiceImpl` | `PaymentServiceImpl` |
| Mapper | `{Domain}Mapper` | `PaymentMapper` |
| 요청 DTO | `{FunctionName}Req` | `PaymentApprovalReq` |
| 응답 DTO | `{FunctionName}Res` | `PaymentApprovalRes` |

---

## 3. 서비스 메소드 명명

```
CREATE → insert{Entity}{Action}()   예: insertPaymentApproval()
READ   → select{Entity}{Action}()   예: selectPaymentHistory()
UPDATE → update{Entity}{Action}()   예: updatePaymentStatus()
DELETE → delete{Entity}{Action}()   예: deletePaymentRecord()
```

---

## 4. 응답 템플릿

- `ResponseTemplate` 과 `ResultCode` 는 모듈 내 자체 정의한다.
- REST API 프로젝트의 `common` 모듈 또는 타 모듈의 `ResponseTemplate` 을 공유하지 않는다.

```java
// ✅ 모듈 자체 ResponseTemplate 사용
return new ResponseTemplate(ResultCode.SUCCESS, data);
```

---

## 5. Mapper 메소드 명명 규칙

DB 연동이 있는 경우 `{tableNameCamelCase}{C|R|U|D}{###}` 패턴을 따른다.

```
R = SELECT : paymentInfoR001(PaymentInfoR001In in)  → PaymentInfoR001Out
C = INSERT : paymentInfoC001(PaymentInfoC001In in)  → int
U = UPDATE : paymentInfoU001(PaymentInfoU001In in)  → int
D = DELETE : paymentInfoD001(PaymentInfoD001In in)  → int
```

---

## 6. Lombok 어노테이션 규칙

| 클래스 유형 | 어노테이션 |
|------------|-----------|
| 요청 DTO | `@Getter @Setter @NoArgsConstructor @ToString` |
| 응답 DTO | `@Getter @Builder @ToString @JsonInclude(JsonInclude.Include.NON_NULL)` |
| MyBatis In DTO | `@Getter @Builder @ToString @Alias("lowerCamelCase클래스명")` |
| MyBatis Out DTO | `@Getter @Setter @ToString @Alias("lowerCamelCase클래스명")` |

---

## 7. 프론트엔드 개발 규칙

프론트엔드(JavaScript, CSS, Thymeleaf) 개발 시 반드시 별도 프론트엔드 가이드를 참조한다.

- **가이드 위치**:
  - [guide-frontend/common.md](guide-frontend/common.md) — 공통 규칙 (디렉토리, AJAX, DOM, 라이브러리, Thymeleaf, CSS, 보안)
  - IIFE + UI 네임스페이스 패턴 적용 프로젝트 → [guide-frontend/iife-pattern.md](guide-frontend/iife-pattern.md)
  - MVC 패턴 적용 프로젝트 → [guide-frontend/mvc-pattern.md](guide-frontend/mvc-pattern.md)
- **적용 대상**: Spring Boot Web(JAR) 프로젝트의 정적 리소스 (프로젝트 프런트엔드 가이드 참조)
- **핵심 사항**:
  - IIFE + `UI` 네임스페이스 패턴 스코프: 프로젝트 프런트엔드 가이드 참조. ES6 class 허용 여부는 프로젝트별로 다름
  - jQuery 플러그인 패턴 적용 스코프는 **프로젝트 프런트엔드 가이드** 참조
  - 프로젝트 UI 네임스페이스 프레임워크(`UI.modal`, `UI.tab`, `UI.page` 등)는 해당 스코프에서만 사용 (프로젝트 프런트엔드 가이드 참조)

---

## 8. 코드리뷰 (프론트엔드)

`/code-review`·`/mr-review` 가 본 가이드 대상의 프론트엔드 변경을 리뷰할 때 참조하는 섹션.

- **정적 리소스 경로**: `src/main/resources/static/`
- **템플릿 경로**: `src/main/resources/templates/`
- **리뷰 패턴 표**: `.claude/skills/code-review/references/frontend-patterns.md` (FE 공통 점검 규칙)
- 프론트엔드 파일(`*.js`/`*.css`/`*.html`) 감지 시 위 경로 기준으로 범위를 잡고 `frontend-patterns.md` + `guide-frontend/*` 규칙을 적용한다.

---

## 코드리뷰 평가 (code-review)

`/code-review`·`/mr-review` 가 본 가이드 대상 백엔드 변경을 리뷰할 때 적용하는 유형별 평가 항목. (프론트엔드 평가는 §8 + `frontend-patterns.md` 참조)

<type-eval>

- Lombok DTO 규칙 (Req/Res/In/Out) · `@Alias` (MyBatis DTO)
- `ResponseTemplate` 모듈 자체 정의
- Mapper 메소드명

</type-eval>
