# 코드리뷰 보강 — Java

## 추가 평가 항목
- JUnit 테스트 클래스 누락

## 추가 평가 항목 — MyBatis

- MyBatis `@Alias` 사용 적정성

## 추가 평가 항목 — Spring

- `@Transactional` 범위(과도/누락)

## 교정 사례 (severity) — Spring

| 사례 | 등급 |
|---|---|
| `@Transactional` on private method | C10 |
| `@Transactional` 메서드 내 `RestTemplate.exchange()` | RES-02 |
