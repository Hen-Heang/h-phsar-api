---
title: 마켓플레이스
type: domain
domain: marketplace
updated: 2026-09-02
---

H-Phsar는 B2B 도매 마켓플레이스다("Phsar"는 크메르어로 "시장"). 공급자(SUPPLIER)가 스토어를 열고 상품·재고를 관리하며, 구매자(BUYER)가 스토어를 탐색해 주문하고, 관리자(ADMIN)가 계정을 모더레이션한다. 전체가 `h-phsar-api` 단일 모듈로 구현되어 있다([[modules/h-phsar-api]] 참고).

## 개요

- **인증·계정**: 이메일/비밀번호 가입 + OTP 이메일 인증, JWT 로그인 + refresh 토큰 회전/재사용 탐지, 관리자 계정 활성화.
- **구매자 흐름**: 스토어 탐색(하이브리드 검색)·북마크·평점 → 장바구니(스토어당 1개 제약) → 주문 제출(CART→DRAFT→PENDING) → 수령확인(DISPATCHED→COMPLETED).
- **공급자 흐름**: 스토어/카테고리/상품(재고 누적) 관리 → 대기주문 수락(재고 차감과 원자적 트랜잭션)/거절 → 준비→배송 → (수령확인은 구매자 전용으로 이관, 공급자 측 `orderDelivered`는 레거시 라우트).

## 흐름

주문 생애주기 전체는 `OrderStatus`(`CART → DRAFT → PENDING → PROCESSING → DISPATCHED → COMPLETED`, 또는 `REJECTED`/`CANCELLED`)와 중앙화된 `OrderStatusService`(guarded UPDATE)로 관리된다. 자세한 상태별 흐름은 [[modules/h-phsar-api]]의 "## 흐름" 절과 `docs/ORDER_WORKFLOW.md`(리포지토리 루트) 참고.

## 관련
- [[modules/h-phsar-api]]
- [[common]]

<!-- domain-tables:begin -->
- 소속 모듈: h-phsar-api
- 테이블 (상위 20): TB_SUPPLIER_ACCOUNT, TB_STORE, TB_CATEGORY, TB_ROLE, TB_STORE_PRODUCT_DETAIL, TB_STORE_CATEGORY, TB_ORDER, TB_ORDER_DETAIL, TB_RATING_DETAIL, TB_BOOKMARK, TB_PRODUCT, TB_STORE_PHONE, TB_BUYER_ACCOUNT, TB_BUYER_INFO, TB_BUYER_PHONE, TB_SUPPLIER_INFO, TB_BUYER_NOTIFICATION, TB_NOTIFICATION_TYPE, TB_ORDER_STATUS_HISTORY, TB_STATUS …외12
<!-- domain-tables:end -->
