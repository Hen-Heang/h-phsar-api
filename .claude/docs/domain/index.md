---
title: 도메인 색인
type: index
updated: 2026-09-02
---

H-Phsar(`h-phsar-api`) 도메인 분석 번들의 진입점. 소스를 탐색하기 전 이 페이지 → 대상 모듈/도메인 페이지 순으로 참조한다.

## 매트릭스

| 모듈 | 도메인 |
|---|---|
| h-phsar-api | [[domains/marketplace]] |

## 페이지 목록

| 페이지 | 종류 | 설명 |
|---|---|---|
| [[modules/h-phsar-api]] | module | 유일한 물리 모듈 — 인증/구매자/공급자 3영역을 함께 담당 |
| [[domains/marketplace]] | domain | H-Phsar 마켓플레이스 도메인 개요 |
| [[common]] | common | 플랫폼 공통 관행(§0) |
| taxonomy.md | taxonomy | 도메인 분류 체계(등재값: marketplace) |
| tables/catalog.md | tables | 테이블별 역방향 색인 |

## 소스맵

- 진입점: `src/main/java/com/henheang/hphsar/controller/**` (컨트롤러 20개, REST + Thymeleaf 뷰)
- 서비스: `src/main/java/com/henheang/hphsar/service/**`
- 매퍼: `src/main/resources/mapper/**` (MyBatis XML)
- 스키마: `src/main/resources/script/schema.sql`
- 프론트: `src/main/resources/templates/**`, `src/main/resources/static/**`

## 모듈 커버리지

| 모듈(경로) | 티어 | 방문 | 룰 | 흐름 | 비고 |
|-----------|------|:---:|:--:|:---:|------|
| h-phsar-api | 도메인 | ✓ | 28 | 9 | 단일 모듈, 3개 병렬 서브에이전트 배치(인증/구매자/공급자)로 전수 방문 |

<!-- catalog-ledger:begin -->
| 모듈(경로) | 카탈로그 행 | 의미 채움 | 테이블 수 |
|---|---|---|---|
| h-phsar-api | 121 | 74/121 | 32 |
<!-- catalog-ledger:end -->

<!-- domain-tables-index:begin -->
| 도메인 | 핵심 테이블 (상위 10) |
|---|---|
| marketplace | TB_SUPPLIER_ACCOUNT, TB_STORE, TB_CATEGORY, TB_ROLE, TB_STORE_PRODUCT_DETAIL, TB_STORE_CATEGORY, TB_ORDER, TB_ORDER_DETAIL, TB_RATING_DETAIL, TB_BOOKMARK …외22 |
<!-- domain-tables-index:end -->

## 관련
- [[modules/h-phsar-api]]
- [[domains/marketplace]]
- [[common]]
