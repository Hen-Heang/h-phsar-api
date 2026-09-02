# 개발 가이드 — Proxy / Router

> **적용 대상:** Proxy·Router 프로젝트
> **유형:** Spring Boot 기반 프록시·라우팅 서비스
> **단일 출처:** `.claude/config/project.yaml` `projects[]`

---

## 1. 패키지 구조

기본 패키지는 `{{config.baseNamespacePattern}}.{project}` 이며, 라우팅·프록시 로직 중심으로 구성한다.

```
{{config.baseNamespacePattern}}.{project}
  ├── {Project}Application.java
  ├── config/          ← Spring, Security, Route 설정
  ├── constants/       ← ResultCode, Constants (모듈 자체 정의)
  ├── filter/          ← 요청/응답 필터
  ├── handler/         ← 전역 예외 핸들러
  └── model/           ← 요청/응답 모델 (필요 시)
```

---

## 2. 클래스 명명 규칙

역할 기반으로 명명한다.

| 역할 | 패턴 | 예시 |
|------|------|------|
| 필터 | `{Domain}Filter` | `AuthFilter`, `RequestLoggingFilter` |
| 핸들러 | `{Domain}Handler` | `GlobalExceptionHandler`, `RouteHandler` |
| 설정 | `{Domain}Config` | `RouteConfig`, `SecurityConfig` |

---

## 3. 특징

- **라우팅·프록시 중심** — Service 레이어가 없거나 경량화되어 있다.
- **Factory 패턴 불필요** — 단일 라우팅 흐름이 일반적이며, 다중 백엔드 분기가 필요한 경우에만 적용한다.
- REST API 프로젝트의 `common` 모듈 `ResponseTemplate` / `ResponseCode` 를 공유하지 않는다.

---

## 4. 응답 템플릿

`ResponseTemplate` 과 `ResultCode` 는 모듈 내 자체 정의한다.

```java
// 모듈 내 자체 정의 예시
public class ResponseTemplate {
    private String resultCode;
    private String resultMsg;
    private Object data;
    // ...
}
```

---

## 코드리뷰 평가 (code-review)

`/code-review`·`/mr-review` 가 본 가이드 대상 변경을 리뷰할 때 적용하는 유형별 평가 항목.

<type-eval>

- `ResponseTemplate` 모듈 자체 정의
- MDC `{{config.tracing.mdcKey}}` 적용

</type-eval>
