---
title: h-phsar-api
type: module
product: h-phsar-api
domain: marketplace
updated: 2026-09-02
---

# h-phsar-api

B2B 도매 마켓플레이스(H-Phsar)의 단일 Spring Boot 모듈. 인증·계정관리, 구매자(BUYER) 장보기·주문, 공급자(SUPPLIER) 스토어·재고·주문이행을 한 애플리케이션이 함께 담당한다.

## 역할·구조

- **인증·계정관리**: `JwtAuthenticationController`(로그인/회원가입/비밀번호), `OTPController`(이메일 인증), `AdminAccountController`(관리자의 buyer/supplier 계정 활성화), `FileController`(파일 저장), `WebViewController`(Thymeleaf 뷰 shell).
- **구매자(BUYER)**: `BuyerOrderController`(장바구니→주문 생애주기), `BuyerStoreController`(스토어 탐색·북마크·평점), `BuyerHistoryController`(초안/주문 이력), `BuyerNotificationController`, `BuyerProfileController`, `BuyerReportController`.
- **공급자(SUPPLIER)**: `SupplierOrderController`(주문 이행: 수락/거절→준비→배송), `SupplierProductController`(상품·재고), `CategoryController`, `SupplierStoreController`, `SupplierHistoryController`, `SupplierHomepageController`, `SupplierNotificationController`, `SupplierProfileController`, `SupplierReportController`.
- 계층: Controller → Service(`*ServiceImpl`) → Repository 인터페이스 → Mapper XML → PostgreSQL. 주문 상태 전이는 `OrderStatusService`로 중앙화되어 "가드된 UPDATE + 영향행 확인" 패턴을 표준으로 쓴다([[common]] 참고).

## 카탈로그
<!-- catalog:begin -->
범례 — 표의 `T#`은 아래 테이블 그룹, `F#:줄`은 아래 근거 파일을 뜻한다:
- T1: TB_BUYER_ACCOUNT, TB_BUYER_INFO, TB_SUPPLIER_ACCOUNT, TB_SUPPLIER_INFO
- T2: TB_BOOKMARK, TB_BUYER_ACCOUNT, TB_BUYER_INFO, TB_BUYER_PHONE, TB_CATEGORY, TB_ORDER, TB_ORDER_DETAIL, TB_ORDER_STATUS_HISTORY, TB_PRODUCT, TB_PRODUCT_IMPORT, TB_PRODUCT_IMPORT_DETAIL, TB_RATING_DETAIL, TB_ROLE, TB_STATUS, TB_STORE, TB_STORE_CATEGORY, TB_STORE_PHONE, TB_STORE_PRODUCT_DETAIL, TB_SUPPLIER_ACCOUNT
- T3: TB_BOOKMARK, TB_BUYER_NOTIFICATION, TB_CATEGORY, TB_NOTIFICATION_TYPE, TB_ORDER, TB_ORDER_DETAIL, TB_PRODUCT, TB_RATING_DETAIL, TB_ROLE, TB_STORE, TB_STORE_CATEGORY, TB_STORE_PHONE, TB_STORE_PRODUCT_DETAIL, TB_SUPPLIER_ACCOUNT, TB_SUPPLIER_NOTIFICATION
- T4: TB_BOOKMARK, TB_BUYER_ACCOUNT, TB_BUYER_INFO, TB_BUYER_NOTIFICATION, TB_BUYER_PHONE, TB_CATEGORY, TB_NOTIFICATION_TYPE, TB_ORDER, TB_ORDER_DETAIL, TB_ORDER_STATUS_HISTORY, TB_PRODUCT, TB_PRODUCT_CATEGORY, TB_PRODUCT_IMPORT, TB_PRODUCT_IMPORT_DETAIL, TB_RATING_DETAIL, TB_ROLE, TB_STATUS, TB_STORE, TB_STORE_CATEGORY, TB_STORE_PHONE, TB_STORE_PRODUCT_DETAIL, TB_SUPPLIER_ACCOUNT, TB_SUPPLIER_INFO, TB_SUPPLIER_NOTIFICATION, TB_SUPPLIER_PHONE
- T5: TB_BOOKMARK, TB_BUYER_ACCOUNT, TB_BUYER_INFO, TB_BUYER_PHONE, TB_CATEGORY, TB_ORDER, TB_ORDER_DETAIL, TB_PRODUCT, TB_RATING_DETAIL, TB_ROLE, TB_STORE, TB_STORE_CATEGORY, TB_STORE_PHONE, TB_STORE_PRODUCT_DETAIL, TB_SUPPLIER_ACCOUNT
- T6: TB_BOOKMARK, TB_CATEGORY, TB_ORDER, TB_ORDER_DETAIL, TB_PRODUCT, TB_RATING_DETAIL, TB_ROLE, TB_STORE, TB_STORE_CATEGORY, TB_STORE_PHONE, TB_STORE_PRODUCT_DETAIL, TB_SUPPLIER_ACCOUNT
- T7: SET, TB_CATEGORY, TB_STORE, TB_STORE_CATEGORY, TB_STORE_PRODUCT_DETAIL
- T8: TB_ADMIN_ACCOUNT, TB_BUYER_ACCOUNT, TB_BUYER_INFO, TB_BUYER_OTP, TB_BUYER_PHONE, TB_REFRESH_TOKEN, TB_ROLE, TB_SUPPLIER_ACCOUNT, TB_SUPPLIER_INFO, TB_SUPPLIER_OTP, TB_SUPPLIER_PHONE
- T9: TB_BUYER_ACCOUNT, TB_BUYER_OTP, TB_ROLE, TB_SUPPLIER_ACCOUNT, TB_SUPPLIER_OTP
- T10: CREATED_DATE, TB_ORDER, TB_ORDER_DETAIL, TB_PRODUCT_IMPORT, TB_PRODUCT_IMPORT_DETAIL, TB_STORE
- T11: TB_BOOKMARK, TB_BUYER_NOTIFICATION, TB_CATEGORY, TB_NOTIFICATION_TYPE, TB_ORDER, TB_ORDER_DETAIL, TB_ORDER_STATUS_HISTORY, TB_PRODUCT, TB_RATING_DETAIL, TB_ROLE, TB_STATUS, TB_STORE, TB_STORE_CATEGORY, TB_STORE_PHONE, TB_STORE_PRODUCT_DETAIL, TB_SUPPLIER_ACCOUNT, TB_SUPPLIER_NOTIFICATION
- T12: SET, TB_BOOKMARK, TB_CATEGORY, TB_ORDER, TB_ORDER_DETAIL, TB_PRODUCT, TB_PRODUCT_CATEGORY, TB_PRODUCT_IMPORT, TB_PRODUCT_IMPORT_DETAIL, TB_RATING_DETAIL, TB_ROLE, TB_STORE, TB_STORE_CATEGORY, TB_STORE_PHONE, TB_STORE_PRODUCT_DETAIL, TB_SUPPLIER_ACCOUNT
- T13: TB_STORE, TB_SUPPLIER_INFO, TB_SUPPLIER_PHONE
- T14: TB_ADMIN_ACCOUNT, TB_BOOKMARK, TB_BUYER_ACCOUNT, TB_BUYER_INFO, TB_BUYER_PHONE, TB_CATEGORY, TB_ORDER, TB_ORDER_DETAIL, TB_PRODUCT, TB_RATING_DETAIL, TB_ROLE, TB_STORE, TB_STORE_CATEGORY, TB_STORE_PHONE, TB_STORE_PRODUCT_DETAIL, TB_SUPPLIER_ACCOUNT, TB_SUPPLIER_INFO, TB_SUPPLIER_PHONE
- F1: `src/main/java/com/henheang/hphsar/controller/admin/account/AdminAccountController.java`
- F2: `src/main/java/com/henheang/hphsar/controller/buyer/history/BuyerHistoryController.java`
- F3: `src/main/java/com/henheang/hphsar/controller/buyer/notification/BuyerNotificationController.java`
- F4: `src/main/java/com/henheang/hphsar/controller/buyer/order/BuyerOrderController.java`
- F5: `src/main/java/com/henheang/hphsar/controller/buyer/profile/BuyerProfileController.java`
- F6: `src/main/java/com/henheang/hphsar/controller/buyer/store/BuyerStoreController.java`
- F7: `src/main/java/com/henheang/hphsar/controller/supplier/category/CategoryController.java`
- F8: `src/main/java/com/henheang/hphsar/controller/FileController.java`
- F9: `src/main/java/com/henheang/hphsar/controller/JwtAuthenticationController.java`
- F10: `src/main/java/com/henheang/hphsar/controller/otp/OTPController.java`
- F11: `src/main/java/com/henheang/hphsar/controller/supplier/history/SupplierHistoryController.java`
- F12: `src/main/java/com/henheang/hphsar/controller/supplier/homepage/SupplierHomepageController.java`
- F13: `src/main/java/com/henheang/hphsar/controller/supplier/notification/SupplierNotificationController.java`
- F14: `src/main/java/com/henheang/hphsar/controller/supplier/order/SupplierOrderController.java`
- F15: `src/main/java/com/henheang/hphsar/controller/supplier/product/SupplierProductController.java`
- F16: `src/main/java/com/henheang/hphsar/controller/supplier/profile/SupplierProfileController.java`
- F17: `src/main/java/com/henheang/hphsar/controller/supplier/store/SupplierStoreController.java`
- F18: `src/main/java/com/henheang/hphsar/controller/web/WebViewController.java`

| 진입점 | 종류 | 라우트/트리거 | 테이블 | 조작 | 근거 | 의미 |
|---|---|---|---|---|---|---|
| AdminAccountController.getBuyer | http | ${base.admin.v1}/buyers/{id} | T1 | select,update | F1:27 | 존재하지 않는 buyer id 조회 시 NotFoundException(404) 처리 |
| AdminAccountController.getSupplier | http | ${base.admin.v1}/suppliers/{id} | T1 | select,update | F1:27 | 존재하지 않는 supplier id 조회 시 NotFoundException(404) 처리 |
| AdminAccountController.listBuyers | http | ${base.admin.v1}/buyers | T1 | select,update | F1:27 | pageNumber/pageSize가 null이거나 1 미만이면 400으로 페이징 파라미터를 검증 |
| AdminAccountController.listSuppliers | http | ${base.admin.v1}/suppliers | T1 | select,update | F1:27 | pageNumber/pageSize가 null이거나 1 미만이면 400으로 페이징 파라미터를 검증 |
| AdminAccountController.updateBuyerStatus | http | ${base.admin.v1}/buyers/{id}/status | T1 | select,update | F1:27 | isActive 누락 시 400, 대상 buyer 없음(rowsAffected=0) 시 404, 성공 시 최신 상태를 재조회해 반환하는 활성/비활성 전환 |
| AdminAccountController.updateSupplierStatus | http | ${base.admin.v1}/suppliers/{id}/status | T1 | select,update | F1:27 | isActive 누락 시 400, 대상 supplier 없음(rowsAffected=0) 시 404, 성공 시 최신 상태를 재조회해 반환하는 활성/비활성 전환 |
| BuyerHistoryController.deleteDraftById | http | ${base.buyer.v1}/history/draft/{id} | T2 | delete,insert,select,update | F2:24 | 소유권 스코프 삭제 — 타인 초안이면 404(존재 은닉), 삭제행수 검증 |
| BuyerHistoryController.getDraftHistory | http | ${base.buyer.v1}/history/draft | T2 | delete,insert,select,update | F2:24 | 구매자 프로필 생성 여부 선확인 후 초안 목록 페이징 조회 |
| BuyerHistoryController.getOrderHistory | http | ${base.buyer.v1}/history/order | T2 | delete,insert,select,update | F2:24 | 구매자 프로필 생성 여부 선확인 후 주문 이력 페이징 조회 |
| BuyerHistoryController.updateDraftById | http | ${base.buyer.v1}/history/draft/{id} | T2 | delete,insert,select,update | F2:24 | 초안 제출(DRAFT→PENDING) — 동일 스토어 카트/대기주문 충돌 검사 후 중앙 전이 서비스 호출 |
| BuyerNotificationController.getUserAllNotification | http | ${base.buyer.v1}/notifications | T3 | delete,insert,select | F3:23 | 역할(구매자/공급자)별 알림 테이블 분기 조회 |
| BuyerNotificationController.markAllNotificationAsRead | http | ${base.buyer.v1}/notifications/read | T3 | delete,insert,select | F3:23 | 알림 존재 및 미읽음 여부 확인 후 전체 읽음 처리 |
| BuyerNotificationController.markNotificationAsRead | http | ${base.buyer.v1}/notifications/{id}/read | T3 | delete,insert,select | F3:23 | 소유권+미읽음 여부 확인 후 단건 읽음 처리 |
| BuyerOrderController.addProductToCart | http | ${base.buyer.v1}/orders/cart | T4 | delete,insert,select,update | F4:33 | 인증/프로필/스토어당 단일카트 제약/재고 확인 후 담기 |
| BuyerOrderController.cancelCart | http | ${base.buyer.v1}/orders/cart/cancel | T4 | delete,insert,select,update | F4:33 | 카트 존재 확인 후 영구 삭제 |
| BuyerOrderController.cancelOrder | http | ${base.buyer.v1}/orders/{id}/cancel | T4 | delete,insert,select,update | F4:33 | 소유권+취소가능 상태(DRAFT/PENDING)만 허용, CANCELLED로 전이 |
| BuyerOrderController.confirmOrder | http | ${base.buyer.v1}/orders/confirm | T4 | delete,insert,select,update | F4:33 | CART→PENDING 전이 + 공급자 신규주문 알림 생성 |
| BuyerOrderController.confirmTransaction | http | ${base.buyer.v1}/orders/{id}/receive | T4 | delete,insert,select,update | F4:33 | markOrderAsArrived와 동일한 수령확인(DISPATCHED→COMPLETED) 로직에 위임되는 별칭 라우트 |
| BuyerOrderController.getOrderActivities | http | ${base.buyer.v1}/orders | T4 | delete,insert,select,update | F4:33 | 정렬/페이징된 본인 주문 목록 조회 |
| BuyerOrderController.getOrderDetailByOrderId | http | ${base.buyer.v1}/orders/{id} | T4 | delete,insert,select,update | F4:33 | 소유권 검증(IDOR 방지) 후 주문 상세 조회 |
| BuyerOrderController.getOrderHistory | http | ${base.buyer.v1}/orders/{id}/history | T4 | delete,insert,select,update | F4:33 | 소유권 검증 후 주문 상태 이력 조회 |
| BuyerOrderController.markOrderAsArrived | http | ${base.buyer.v1}/orders/{id}/arrived | T4 | delete,insert,select,update | F4:33 | 소유권+DISPATCHED 상태 검증 후 COMPLETED로 전이(수령확인) |
| BuyerOrderController.removeProductInCart | http | ${base.buyer.v1}/orders/cart/product | T4 | delete,insert,select,update | F4:33 | 카트/상품 존재 확인 후 삭제 |
| BuyerOrderController.saveToDraft | http | ${base.buyer.v1}/orders/cart/draft | T4 | delete,insert,select,update | F4:33 | CART→DRAFT 전이(중앙 전이 서비스) |
| BuyerOrderController.updatePrductInCart | http | ${base.buyer.v1}/orders/cart | T4 | delete,insert,select,update | F4:33 | 재고 확인 후 수량 변경, qty=0이면 삭제 |
| BuyerOrderController.viewAllCarts | http | ${base.buyer.v1}/orders/carts | T4 | delete,insert,select,update | F4:33 | CRUD |
| BuyerOrderController.viewCartDetail | http | ${base.buyer.v1}/orders/cart/details | T4 | delete,insert,select,update | F4:33 | 카트 존재 확인 후 페이징 상세 조회 |
| BuyerOrderController.viewInvoiceByOrderId | http | ${base.buyer.v1}/orders/invoice/{id} | T4 | delete,insert,select,update | F4:33 | 소유권 검증 + COMPLETED 상태만 인보이스 열람 허용 |
| BuyerProfileController.createBuyerProfile | http | ${base.buyer.v1}/profiles | T5 | delete,insert,select,update | F5:24 | 1회성 생성 가드 + 전화번호 중복/형식, 성별 값 검증 |
| BuyerProfileController.getBuyerProfile | http | ${base.buyer.v1}/profiles | T5 | delete,insert,select,update | F5:24 | CRUD |
| BuyerProfileController.updateBuyerProfile | http | ${base.buyer.v1}/profiles | T5 | delete,insert,select,update | F5:24 | 프로필 존재 필수 + 전화번호 형식 재검증 |
| BuyerReportController.getBuyerMonthlyReport | http | ${base.buyer.v1}/reports | CREATED_DATE, O, TB_CATEGORY, TB_ORDER, TB_ORDER_DETAIL, TB_RATING_DETAIL, TB_STORE_PRODUCT_DETAIL | select | `src/main/java/com/henheang/hphsar/controller/buyer/report/BuyerReportController.java:22` | 날짜범위 검증(끝<시작 거부) 후 월/연도별 지출·주문 통계 집계 |
| BuyerStoreController.bookmarkStoreById | http | ${base.buyer.v1}/stores/{storeId}/bookmark | T6 | delete,insert,select | F6:29 | 중복 북마크 방지 가드 |
| BuyerStoreController.deleteRatingByStoreId | http | ${base.buyer.v1}/stores/{storeId}/rating | T6 | delete,insert,select | F6:29 | 평점 존재 여부 확인 후 삭제 |
| BuyerStoreController.editRatingByStoreId | http | ${base.buyer.v1}/stores/{storeId}/rating | T6 | delete,insert,select | F6:29 | 평점 존재 필수 + 1~5 범위 검증 |
| BuyerStoreController.getAllBookmarkedStore | http | ${base.buyer.v1}/stores/bookmark | T6 | delete,insert,select | F6:29 | CRUD |
| BuyerStoreController.getAllStore | http | ${base.buyer.v1}/stores | T6 | delete,insert,select | F6:29 | CRUD |
| BuyerStoreController.getAllUserStoreSortByCurrentUserFavorite | http | ${base.buyer.v1}/stores/sort/favorite | T6 | delete,insert,select | F6:29 | CRUD |
| BuyerStoreController.getAllUserStoreSortByDate | http | ${base.buyer.v1}/stores/sort/date | T6 | delete,insert,select | F6:29 | CRUD |
| BuyerStoreController.getAllUserStoreSortByName | http | ${base.buyer.v1}/stores/sort/name | T6 | delete,insert,select | F6:29 | CRUD |
| BuyerStoreController.getAllUserStoreSortByRatedStar | http | ${base.buyer.v1}/stores/sort/rated | T6 | delete,insert,select | F6:29 | CRUD |
| BuyerStoreController.getCategoryListingByStoreId | http | ${base.buyer.v1}/stores/{storeId}/category | T6 | delete,insert,select | F6:29 | CRUD |
| BuyerStoreController.getProductListingByStoreId | http | ${base.buyer.v1}/stores/{storeId}/products | T6 | delete,insert,select | F6:29 | 정렬 컬럼 화이트리스트(allowlist Map)로만 매핑해 SQL 인젝션 차단 |
| BuyerStoreController.getRatingByStoreId | http | ${base.buyer.v1}/stores/{storeId}/rating | T6 | delete,insert,select | F6:29 | CRUD |
| BuyerStoreController.getStoreById | http | ${base.buyer.v1}/stores/{id} | T6 | delete,insert,select | F6:29 | 비공개(미게시) 스토어는 구매자에게 노출하지 않음 |
| BuyerStoreController.getStoreProductByCategory | http | ${base.buyer.v1}/stores/{storeId}/products/category | T6 | delete,insert,select | F6:29 | 카테고리가 해당 스토어 소속인지 검증 후 상품 조회 |
| BuyerStoreController.getStoresByCategorySearch | http | ${base.buyer.v1}/stores/category/search | T6 | delete,insert,select | F6:29 | CRUD |
| BuyerStoreController.getStoresBySearch | http | ${base.buyer.v1}/stores/hybrid/search | T6 | delete,insert,select | F6:29 | 상품>카테고리>이름 우선순위로 결과를 병합하는 하이브리드 검색 |
| BuyerStoreController.ratingStoreById | http | ${base.buyer.v1}/stores/{storeId}/rating | T6 | delete,insert,select | F6:29 | 중복 평가 방지 + 1~5 범위 검증 |
| BuyerStoreController.removeBookmarkStoreById | http | ${base.buyer.v1}/stores/{storeId}/bookmark/remove | T6 | delete,insert,select | F6:29 | 북마크 미존재 시 충돌 처리(존재 가드) |
| BuyerStoreController.searchStoreByName | http | ${base.buyer.v1}/stores/name/search | T6 | delete,insert,select | F6:29 | CRUD |
| CategoryController.createCategoryStore | http | ${base.supplier.v1}/categories | T7 | insert,select,update | F7:27 | 카테고리명을 정규화(trim/lower) 후 전역에 없으면 생성, 있으면 재사용하되 이미 이 매장에 연결돼 있으면 충돌 처리 |
| CategoryController.deleteCategory | http | ${base.supplier.v1}/categories/{id} | T7 | insert,select,update | F7:27 | 삭제 전 해당 카테고리를 쓰던 상품을 UNKNOWN(113)으로 먼저 이관한 뒤 삭제 |
| CategoryController.editCategory | http | ${base.supplier.v1}/categories/{id} | T7 | insert,select,update | F7:27 | UNKNOWN으로는 변경 불가, 동일명 카테고리가 있으면 병합하고 기존 상품을 새 카테고리로 이관 |
| CategoryController.getAllCategory | http | ${base.supplier.v1}/categories | T7 | insert,select,update | F7:27 | CRUD |
| CategoryController.getAllCategoryOfCurrentStore | http | ${base.supplier.v1}/categories/all | T7 | insert,select,update | F7:27 | CRUD |
| CategoryController.getCategoryById | http | ${base.supplier.v1}/categories/{id} | T7 | insert,select,update | F7:27 | CRUD |
| CategoryController.searchCategoryByName | http | ${base.supplier.v1}/categories/search | T7 | insert,select,update | F7:27 | CRUD |
| FileController.getFile | http | /api/v1/files/{id} |  |  | F8:19 | CRUD |
| FileController.uploadFile | http | /api/v1/files/upload |  |  | F8:19 | CRUD |
| JwtAuthenticationController.changePassword | http | /authorization/change-password | T8 | delete,select,update | F9:44 | 기존 비밀번호가 저장된 BCrypt 해시와 일치해야만 새 비밀번호로 변경 가능 |
| JwtAuthenticationController.createAuthenticationToken | http | /authorization/login | T8 | delete,select,update | F9:44 | 이메일 미인증 시 로그인을 막고 자동으로 새 OTP를 발송하며, 미존재 계정과 비밀번호 오류를 동일 문구로 응답해 이메일 존재 여부 추측(enumeration)을 방지 |
| JwtAuthenticationController.forgetPassword | http | /authorization/forget | T8 | delete,select,update | F9:44 | OTP(이메일/코드 일치, 3분 이내 유효)를 검증해야 비밀번호를 재설정할 수 있고, 사용된 OTP는 즉시 삭제 |
| JwtAuthenticationController.insertUser | http | /authorization/register | T8 | delete,select,update | F9:44 | roleId(1/2)·이메일 형식·비밀번호=확인비밀번호 일치·이메일 중복 여부를 검증한 뒤 BCrypt로 해싱하여 등록 |
| JwtAuthenticationController.logout | http | /authorization/logout | T8 | delete,select,update | F9:44 | refresh token을 서버측에서 폐기(revoke)하고 쿠키를 만료시켜 로그아웃 처리(존재하지 않아도 idempotent하게 성공) |
| JwtAuthenticationController.refreshAccessToken | http | /authorization/refresh | T8 | delete,select,update | F9:44 | 이미 폐기된 refresh token이 재사용되면 탈취로 간주해 해당 계정의 모든 활성 세션을 강제 폐기하는 회전(rotate) 방식 |
| OTPController.activateAccount | http | /authorization/api/v1/otp/verify | T9 | delete,select | F10:26 | OTP 코드/이메일 일치 및 3분 이내 유효성 검증 후 계정을 인증 처리하고 OTP는 1회용으로 즉시 삭제 |
| OTPController.generateOtp | http | /authorization/api/v1/otp/generate | T9 | delete,select | F10:26 | 만료되지 않은 기존 OTP가 있으면 재발급을 막는 rate limiting 적용 |
| SupplierHistoryController.getOrderHistory | http | ${base.supplier.v1}/history/order | T2 | delete,insert,select,update | F11:22 | CRUD |
| SupplierHistoryController.getProductImportHistory | http | ${base.supplier.v1}/history/import | T2 | delete,insert,select,update | F11:22 | CRUD |
| SupplierHomepageController.getNewOrder | http | ${base.supplier.v1}/order_activities | T10 | select | F12:19 | CRUD |
| SupplierHomepageController.months | http | ${base.supplier.v1}/order_activities/months | T10 | select | F12:19 | 기간이 2년 미만이면 월별, 2년 이상이면 연도별 집계로 자동 전환하고 미래/역순 날짜는 거부 |
| SupplierNotificationController.getUserAllNotification | http | ${base.supplier.v1}/notifications | T3 | delete,insert,select | F13:19 | roleId로 구매자/공급자 알림 테이블을 분기 조회 |
| SupplierNotificationController.markAllNotificationAsRead | http | ${base.supplier.v1}/notifications/read | T3 | delete,insert,select | F13:19 | 읽지 않은 알림이 없으면 예외, roleId별로 분기하여 전체 읽음 처리 |
| SupplierNotificationController.markNotificationAsRead | http | ${base.supplier.v1}/notifications/{id}/read | T3 | delete,insert,select | F13:19 | 알림 소유자(현재 사용자) 검증 후 읽음 처리, 없거나 대상이 아니면 예외 |
| SupplierOrderController.acceptPendingOrder | http | ${base.supplier.v1}/orders/pending/accept/{orderId} | T11 | delete,insert,select,update | F14:20 | PENDING만 허용, 상태 전이+재고 차감을 한 트랜잭션으로 묶어 재고 부족 시 수락 자체를 롤백 |
| SupplierOrderController.declinePendingOrder | http | ${base.supplier.v1}/orders/pending/decline/{orderId} | T11 | delete,insert,select,update | F14:20 | PENDING만 허용, PENDING→REJECTED 전이 후 구매자 알림 발송 |
| SupplierOrderController.finishPreparing | http | ${base.supplier.v1}/orders/preparing/{orderId} | T11 | delete,insert,select,update | F14:20 | PROCESSING만 허용, PROCESSING→DISPATCHED 전이 후 배송중 알림 발송 |
| SupplierOrderController.getAllOrderCurrentUserSortByCreatedDate | http | ${base.supplier.v1}/orders | T11 | delete,insert,select,update | F14:20 | CRUD |
| SupplierOrderController.getCompleteOrderCurrentUserSortByCreatedDate | http | ${base.supplier.v1}/orders/complete | T11 | delete,insert,select,update | F14:20 | CRUD |
| SupplierOrderController.getConfirmingOrderCurrentUserSortByCreatedDate | http | ${base.supplier.v1}/orders/confirming | T11 | delete,insert,select,update | F14:20 | CRUD |
| SupplierOrderController.getDispatchingOrderCurrentUserSortByCreatedDate | http | ${base.supplier.v1}/orders/dispatching | T11 | delete,insert,select,update | F14:20 | CRUD |
| SupplierOrderController.getInvoiceByOrderId | http | ${base.supplier.v1}/orders/invoice/{orderId} | T11 | delete,insert,select,update | F14:20 | 주문이 COMPLETED 상태가 아니면 인보이스 발급 거부 |
| SupplierOrderController.getNewOrderCurrentUserSortByCreatedDate | http | ${base.supplier.v1}/orders/pending | T11 | delete,insert,select,update | F14:20 | CRUD |
| SupplierOrderController.getOrderDetailsByOrderId | http | ${base.supplier.v1}/orders/{id}/details | T11 | delete,insert,select,update | F14:20 | CRUD |
| SupplierOrderController.getOrderHistory | http | ${base.supplier.v1}/orders/{orderId}/history | T11 | delete,insert,select,update | F14:20 | CRUD |
| SupplierOrderController.getPreparingOrderCurrentUserSortByCreatedDate | http | ${base.supplier.v1}/orders/preparing | T11 | delete,insert,select,update | F14:20 | CRUD |
| SupplierOrderController.orderDelivered | http | ${base.supplier.v1}/orders/dispatching/{orderId} | T11 | delete,insert,select,update | F14:20 | 폐기된 기능 — 공급자는 더 이상 배송완료 처리를 할 수 없고 항상 ForbiddenException(수령확인은 구매자 전용) |
| SupplierProductController.deleteProductById | http | ${base.supplier.v1}/products/{id} | T12 | delete,insert,select,update | F15:31 | 주문에 이미 사용된 상품은 삭제 불가 |
| SupplierProductController.editProduct | http | ${base.supplier.v1}/products/{id} | T12 | delete,insert,select,update | F15:31 | 수정 시 카테고리가 매장 소속인지 검증하고, 가격 0 금지, 수정 성공 시 항상 강제로 재게시(isPublish=true) |
| SupplierProductController.getAllProductByName | http | ${base.supplier.v1}/products/search | T12 | delete,insert,select,update | F15:31 | CRUD |
| SupplierProductController.getAllProductWithSorting | http | ${base.supplier.v1}/products/sort | T12 | delete,insert,select,update | F15:31 | 정렬 컬럼을 화이트리스트로 검증 후 사용(임의 컬럼명 주입 방지) |
| SupplierProductController.getProductById | http | ${base.supplier.v1}/products/{id} | T12 | delete,insert,select,update | F15:31 | CRUD |
| SupplierProductController.importProduct | http | ${base.supplier.v1}/products/import | T12 | delete,insert,select,update | F15:31 | 재입고 수량은 반드시 양수(0 이하 금지), 재고는 대체가 아닌 누적 증가, 가격 미입력 시 기존가 유지 |
| SupplierProductController.insertNewProduct | http | ${base.supplier.v1}/products | T12 | delete,insert,select,update | F15:31 | 매장당 상품명 중복 금지, 카테고리는 매장 소속이어야 함, 가격 상한 999, 수량이 0이면 자동 미게시 |
| SupplierProductController.publishProduct | http | ${base.supplier.v1}/products/{id}/publish | T12 | delete,insert,select,update | F15:31 | 이미 게시 상태면 충돌 처리, UPDATE 반환 id가 요청 id와 다르면 내부 오류로 방어 |
| SupplierProductController.unPublishProduct | http | ${base.supplier.v1}/products/{id}/unlist | T12 | delete,insert,select,update | F15:31 | 이미 비게시 상태면 충돌 처리, UPDATE 반환 id 불일치 시 내부 오류로 방어 |
| SupplierProfileController.addUserProfile | http | ${base.supplier.v1}/profiles | T13 | delete,insert,select | F16:24 | 프로필 중복 생성 금지, 성별 값은 male/female/other만 허용 |
| SupplierProfileController.getUserProfileById | http | ${base.supplier.v1}/profiles | T13 | delete,insert,select | F16:24 | CRUD |
| SupplierProfileController.updateUserProfile | http | ${base.supplier.v1}/profiles | T13 | delete,insert,select | F16:24 | 프로필이 먼저 생성돼 있어야 수정 가능 |
| SupplierReportController.getSupplierReport | http | ${base.supplier.v1}/reports | TB_BOOKMARK, TB_CATEGORY, TB_ORDER, TB_ORDER_DETAIL, TB_PRODUCT, TB_PRODUCT_IMPORT, TB_PRODUCT_IMPORT_DETAIL, TB_RATING_DETAIL, TB_ROLE, TB_STORE …외4 | delete,insert,select | `src/main/java/com/henheang/hphsar/controller/supplier/report/SupplierReportController.java:22` | 기간이 2년 이상이면 연도별, 미만이면 월별 집계로 분기하고 종료일<시작일이면 거부 |
| SupplierStoreController.createStore | http | ${base.supplier.v1}/stores | T14 | delete,insert,select | F17:21 | 이메일/계정 인증(isVerified) 완료자만 생성 가능, 매장당 1개 제한, 매장명 중복/전화번호 형식/설명 100단어 제한 검증 |
| SupplierStoreController.deleteUserStore | http | ${base.supplier.v1}/stores | T14 | delete,insert,select | F17:21 | 매장 삭제 시 관련 데이터 전체가 함께 삭제됨(존재 검증 후 삭제) |
| SupplierStoreController.disableStore | http | ${base.supplier.v1}/stores/disable | T14 | delete,insert,select | F17:21 | 이미 비활성 상태면 충돌 처리(중복 토글 방지) |
| SupplierStoreController.editAllFieldUserStore | http | ${base.supplier.v1}/stores | T14 | delete,insert,select | F17:21 | 설명 100단어 제한, 전화번호 형식 검증, 추가 전화번호는 전체 삭제 후 재삽입 |
| SupplierStoreController.enableStore | http | ${base.supplier.v1}/stores/enable | T14 | delete,insert,select | F17:21 | 이미 활성 상태면 충돌 처리(중복 토글 방지) |
| SupplierStoreController.getUserStore | http | ${base.supplier.v1}/stores/user | T14 | delete,insert,select | F17:21 | CRUD |
| WebViewController.buyerCart | http | /web/buyers/cart |  |  | F18:9 | CRUD |
| WebViewController.buyerDashboard | http | /web/buyers/dashboard |  |  | F18:9 | CRUD |
| WebViewController.buyerHistory | http | /web/buyers/history |  |  | F18:9 | CRUD |
| WebViewController.buyerOrders | http | /web/buyers/orders |  |  | F18:9 | CRUD |
| WebViewController.buyerProfile | http | /web/buyers/profile |  |  | F18:9 | CRUD |
| WebViewController.buyerStores | http | /web/buyers/stores |  |  | F18:9 | CRUD |
| WebViewController.login | http | /web |  |  | F18:9 | CRUD |
| WebViewController.register | http | /web/register |  |  | F18:9 | CRUD |
| WebViewController.supplierCategories | http | /web/suppliers/categories |  |  | F18:9 | CRUD |
| WebViewController.supplierDashboard | http | /web/suppliers/dashboard |  |  | F18:9 | CRUD |
| WebViewController.supplierOrders | http | /web/suppliers/orders |  |  | F18:9 | CRUD |
| WebViewController.supplierProducts | http | /web/suppliers/products |  |  | F18:9 | CRUD |
| WebViewController.supplierProfile | http | /web/suppliers/profile |  |  | F18:9 | CRUD |
| WebViewController.supplierStore | http | /web/suppliers/store |  |  | F18:9 | CRUD |
<!-- catalog:end -->

## 핵심

### 인증·계정관리
- [확실][고유] 로그인 시 이메일 미존재와 비밀번호 오류를 동일한 에러 문구로 응답해 이메일 열거(enumeration) 공격을 방지 — 근거 `src/main/java/com/henheang/hphsar/service/implement/JwtUserDetailsServiceImpl.java:313` "throw new BadRequestException(\"Invalid email or password.\");"
- [확실][고유] refresh token이 이미 폐기된 상태로 재사용되면 토큰 탈취로 간주해 해당 계정의 모든 활성 세션을 강제 폐기 — 근거 `src/main/java/com/henheang/hphsar/service/implement/RefreshTokenServiceImpl.java:64` "refreshTokenRepository.revokeAllActiveByEmail(found.getEmail());"
- [확실][표준] refresh token 회전(rotate)은 guarded UPDATE로 처리해 동시 요청 경쟁 시 하나만 성공하도록 affected-row count를 검증 — 근거 `src/main/java/com/henheang/hphsar/service/implement/RefreshTokenServiceImpl.java:79` "if (rowsRevoked == null || rowsRevoked == 0) {"
- [확실][고유] OTP 재발급은 기존 OTP가 만료되지 않았으면 새 발급을 차단하는 rate limiting을 적용 — 근거 `src/main/java/com/henheang/hphsar/service/implement/OtpServiceImplV1.java:95` "if (existingOtp != null && OtpUtils.isNotExpired(existingOtp.getCreatedDate())) {"
- [확실][표준] OTP는 검증 성공 즉시 DB에서 삭제되어 1회용으로만 사용 가능 — 근거 `src/main/java/com/henheang/hphsar/service/implement/OtpServiceImplV1.java:181` "otpRepository.deleteSupplierOtp(email);"
- [확실][고유] 관리자의 계정 상태(활성/비활성) 변경은 영향받은 row 수가 0이면 대상 없음(404)으로 처리 — 근거 `src/main/java/com/henheang/hphsar/service/implement/AdminAccountServiceImpl.java:48` "if (rowsAffected == null || rowsAffected == 0) {"
- [확실][고유] 이메일 미인증 계정의 로그인은 차단되고 자동으로 새 OTP가 발송됨 — 근거 `src/main/java/com/henheang/hphsar/service/implement/JwtUserDetailsServiceImpl.java:317` "otpService.generateOtp(email);"
- [확실][표준] 비밀번호 변경은 기존 비밀번호가 저장된 BCrypt 해시와 일치해야만 허용 — 근거 `src/main/java/com/henheang/hphsar/service/implement/JwtUserDetailsServiceImpl.java:205` "if (!passwordEncoder.matches(request.getOldPassword(), appUser.getPassword())) {"
- [확실][고유] 회원가입 화면은 비밀번호/확인비밀번호를 클라이언트에서 수집·검증하지만, `POST /authorization/register` 요청 바디는 반드시 `confirmPassword` 필드를 포함해야 한다 — 누락 시 서버 검증이 항상 "Password and confirm password do not match."로 실패한다(2026-09-02 register.html 수정으로 반영됨) — 근거 `src/main/resources/templates/auth/register.html:253` "await api.post('/authorization/register', { email, password, confirmPassword, roleId: selectedRoleId });"

### 구매자(BUYER)
- [확실][고유] 구매자는 다른 스토어에 이미 카트가 있으면 새 스토어에서 담기를 할 수 없다(스토어당 활성 카트 1개 제약) — 근거 `src/main/java/com/henheang/hphsar/service/implement/BuyerOrderServiceImpl.java:64` "if (buyerOrderRepository.checkForCartInOtherStore(storeId, buyerId)) {"
- [추정][고유] 담기/수량변경 시 재고(tb_store_product_detail.qty) 확인 가드는 사전 체크일 뿐이며, 실제 원자적 차감은 공급자의 주문 수락 처리에서 이뤄진다 — 근거 `src/main/java/com/henheang/hphsar/service/implement/BuyerOrderServiceImpl.java:89` "if (!buyerOrderRepository.checkStock(productId, qty)) {"
- [확실][표준] 주문 상태 전이는 OrderStatus.canTransitionTo로 유효성 검사 후, DB의 조건부 UPDATE(updateStatusIfCurrent)가 동시성 가드 역할을 한다 — 근거 `src/main/java/com/henheang/hphsar/service/implement/OrderStatusServiceImpl.java:52` "int updated = orderStatusRepository.updateStatusIfCurrent(orderId, current.name(), next.name());"
- [확실][고유] 구매자의 수령확인(COMPLETED 전이)은 본인 소유이면서 DISPATCHED 상태인 주문에만 허용된다(소유권+상태 동시 검증으로 IDOR 방지) — 근거 `src/main/java/com/henheang/hphsar/service/implement/BuyerOrderServiceImpl.java:361` "if (!buyerOrderRepository.checkForDispatchingOrder(id, buyerId)) {"
- [확실][고유] 주문 취소는 본인 소유이면서 DRAFT 또는 PENDING 상태인 주문에만 허용된다 — 근거 `src/main/java/com/henheang/hphsar/service/implement/BuyerOrderServiceImpl.java:378` "if (!buyerOrderRepository.checkOrderExistForBuyerCancellable(id, buyerId)) {"
- [확실][고유] 정렬 대상 컬럼은 화이트리스트 Map으로만 허용되어 사용자 입력이 SQL 컬럼명으로 그대로 쓰이지 않는다 — 근거 `src/main/java/com/henheang/hphsar/service/implement/BuyerStoreServiceImpl.java:231` "Map<String, String> validColumns = Map.of("
- [확실][고유] 미게시(비공개) 스토어는 구매자 조회에서 숨겨진다 — 근거 `src/main/java/com/henheang/hphsar/service/implement/BuyerStoreServiceImpl.java:76` "if (!store.getIsPublish()) {"
- [확실][고유] 스토어 평점은 1~5 범위만 허용되고, 스토어당 1회만 등록 가능(중복 평가 방지) — 근거 `src/main/java/com/henheang/hphsar/service/implement/BuyerStoreServiceImpl.java:206` "if (!(storeRatingRequest.getRatedStar() > 0 && storeRatingRequest.getRatedStar() < 6)) {"
- [확실][고유] 초안을 PENDING으로 제출하기 전, 같은 스토어에 이미 카트/대기중인 주문이 있으면 제출을 거부한다(동시 주문 1건 제약) — 근거 `src/main/java/com/henheang/hphsar/service/implement/HistoryServiceImplV1.java:141` "if (buyerOrderRepository.checkForCartOrPending(storeId, currentUserId, id)) {"
- [확실][고유] 인보이스 열람은 본인 소유이면서 주문이 COMPLETED(status_id=6) 상태일 때만 허용된다 — 근거 `src/main/java/com/henheang/hphsar/service/implement/BuyerOrderServiceImpl.java:305` "if (!checkOrderIsComplete(id)) {"

### 공급자(SUPPLIER)
- [확실][고유] 재고 차감은 원자적 guarded UPDATE(`qty >= quantity`) 성공(영향행 1건) 여부로만 최종 확정되고, 사전 카운트 비교(checkAvailableProduct)는 참고용 빠른 실패일 뿐 동시성 하에서 신뢰할 수 없다 — 근거 `src/main/resources/mapper/SupplierOrderMapper.xml:380` "WHERE id = #{storeProductId} AND qty >= #{quantity}"
- [확실][고유] 주문 상태 전이는 현재 상태를 WHERE 조건에 건 guarded UPDATE 1건이 성공할 때만 확정되며, 실패하면 ConflictException으로 동시 변경을 감지한다 — 근거 `src/main/resources/mapper/OrderStatusMapper.xml:30` "WHERE id = #{orderId} AND status_id = (SELECT id FROM tb_status WHERE name = #{currentStatus})"
- [확실][고유] acceptPendingOrder는 상태 전이(PENDING→PROCESSING)와 재고 차감을 같은 @Transactional 경계에 묶어, 재고가 부족하면 주문 수락 자체도 함께 롤백된다 — 근거 `src/main/java/com/henheang/hphsar/service/implement/SupplierOrderServiceImpl.java:273` "@Transactional"
- [확실][고유] 공급자는 배송(DISPATCHED) 이후 주문 완료 처리 권한이 없다 — orderDelivered는 항상 ForbiddenException을 던지고, 수령 확인은 구매자 전용으로 이관되었다(레거시 라우트로만 존재) — 근거 `src/main/java/com/henheang/hphsar/service/implement/SupplierOrderServiceImpl.java:424` "throw new ForbiddenException(\"Only the buyer may confirm receipt of a dispatched order. Supplier responsibility ends at dispatch.\");"
- [확실][고유] 주문 접근 전 항상 store_id 기준 EXISTS로 소유권을 검증해 타 매장 주문 접근을 차단한다 — 근거 `src/main/resources/mapper/SupplierOrderMapper.xml:320` "SELECT EXISTS(SELECT * FROM tb_order WHERE id = #{orderId} AND store_id = #{storeId})"
- [확실][고유] 재입고 수량은 반드시 양수여야 하며, 재입고 API를 통해 몰래 재고를 차감(0 이하 값)하는 것을 막는 방어다 — 근거 `src/main/java/com/henheang/hphsar/service/implement/SupplierProductServiceImpl.java:408` "if (productImport.getQty() == null || productImport.getQty() <= 0) {"
- [확실][고유] 상품 등록/재입고 시 재고는 대체(overwrite)가 아니라 기존 수량에 누적된다 — 근거 `src/main/resources/mapper/SupplierProductMapper.xml:242` "SET qty        = qty + #{qty},"
- [확실][고유] 상품 발행/미발행 토글은 이미 같은 상태면 ConflictException으로 막고, UPDATE가 반환한 id가 요청 id와 다르면 InternalServerErrorException으로 방어한다 — 근거 `src/main/java/com/henheang/hphsar/service/implement/SupplierProductServiceImpl.java:330` "if (supplierProductRepository.checkProductPublish(storeId, id)){"
- [확실][고유] 카테고리 삭제 시 해당 카테고리를 쓰던 상품을 먼저 UNKNOWN(id=113) 카테고리로 이관한 뒤 삭제한다 — 근거 `src/main/java/com/henheang/hphsar/service/implement/CategoryServiceImplV1.java:127` "categoryRepository.createNewStoreCategory(storeId, 113);"
- [확실][고유] 매장 생성은 계정 이메일 인증(isVerified)이 완료된 사용자만 허용된다 — 근거 `src/main/java/com/henheang/hphsar/service/implement/SupplierStoreServiceImpl.java:79` "if (appUser.getIsVerified()) {"

## 흐름

### 인증·계정관리
- 회원가입 → OTP 인증: `src/main/java/com/henheang/hphsar/controller/JwtAuthenticationController.java:75`(insertUser) → `src/main/java/com/henheang/hphsar/service/implement/JwtUserDetailsServiceImpl.java:111`(이메일 형식/중복/비밀번호 확인 검증 후 미인증 상태로 계정 생성) → `src/main/java/com/henheang/hphsar/controller/otp/OTPController.java:44`(generateOtp) → `src/main/java/com/henheang/hphsar/service/implement/OtpServiceImplV1.java:82`(rate limiting 후 코드 발급) → `src/main/java/com/henheang/hphsar/controller/otp/OTPController.java:60`(activateAccount) → `src/main/java/com/henheang/hphsar/service/implement/OtpServiceImplV1.java:134`(검증 후 OTP 삭제).
- 로그인 → JWT 발급 → refresh 회전: `src/main/java/com/henheang/hphsar/controller/JwtAuthenticationController.java:93`(createAuthenticationToken) → `src/main/java/com/henheang/hphsar/service/implement/JwtUserDetailsServiceImpl.java:308`(enumeration 방지) → `src/main/java/com/henheang/hphsar/service/implement/RefreshTokenServiceImpl.java:40`(refresh 쿠키 발급) → `src/main/java/com/henheang/hphsar/controller/JwtAuthenticationController.java:124`(refreshAccessToken) → `src/main/java/com/henheang/hphsar/service/implement/RefreshTokenServiceImpl.java:53`(회전 및 재사용 탐지).
- 관리자 계정 모더레이션: `src/main/java/com/henheang/hphsar/controller/admin/account/AdminAccountController.java:49`(updateSupplierStatus) → `src/main/java/com/henheang/hphsar/service/implement/AdminAccountServiceImpl.java:43`(guarded UPDATE → rowsAffected 확인 → 재조회) → `src/main/resources/mapper/AdminAccountMapper.xml:78`(UPDATE tb_supplier_account SET is_active = #{isActive}).

### 구매자(BUYER)
- 장바구니 담기 → 주문 확정 → 배송 완료 수령확인: `src/main/java/com/henheang/hphsar/controller/buyer/order/BuyerOrderController.java:39`(addProductToCart) → `src/main/java/com/henheang/hphsar/service/implement/BuyerOrderServiceImpl.java:89`(재고 확인) → `src/main/java/com/henheang/hphsar/controller/buyer/order/BuyerOrderController.java:86`(confirmOrder) → `src/main/java/com/henheang/hphsar/service/implement/BuyerOrderServiceImpl.java:238`(CART→PENDING) → `src/main/java/com/henheang/hphsar/controller/buyer/order/BuyerOrderController.java:106`(markOrderAsArrived) → `src/main/java/com/henheang/hphsar/service/implement/BuyerOrderServiceImpl.java:364`(DISPATCHED→COMPLETED).
- 카트를 초안으로 저장 → 초안 제출 → 이력 확인: `src/main/java/com/henheang/hphsar/controller/buyer/order/BuyerOrderController.java:80`(saveToDraft) → `src/main/java/com/henheang/hphsar/service/implement/BuyerOrderServiceImpl.java:225`(CART→DRAFT) → `src/main/java/com/henheang/hphsar/controller/buyer/history/BuyerHistoryController.java:51`(updateDraftById) → `src/main/java/com/henheang/hphsar/service/implement/HistoryServiceImplV1.java:144`(DRAFT→PENDING) → `src/main/java/com/henheang/hphsar/controller/buyer/order/BuyerOrderController.java:134`(getOrderHistory).
- 스토어 하이브리드 검색 → 카테고리별 상품 조회 → 담기: `src/main/java/com/henheang/hphsar/controller/buyer/store/BuyerStoreController.java:166`(getStoresBySearch) → `src/main/java/com/henheang/hphsar/service/implement/BuyerStoreServiceImpl.java:526`(product/category/name 결과 병합) → `src/main/java/com/henheang/hphsar/controller/buyer/store/BuyerStoreController.java:152`(getStoreProductByCategory) → `src/main/java/com/henheang/hphsar/controller/buyer/order/BuyerOrderController.java:39`(addProductToCart).

### 공급자(SUPPLIER)
- 상품 등록/재입고 → 재고 확보 → 주문 수락 시 재고 차감: `src/main/java/com/henheang/hphsar/controller/supplier/product/SupplierProductController.java:104`(importProduct) → `src/main/java/com/henheang/hphsar/service/implement/SupplierProductServiceImpl.java:429`(누적 UPDATE) → `src/main/java/com/henheang/hphsar/service/implement/SupplierOrderServiceImpl.java:299`(deductStockForOrder) → `src/main/resources/mapper/SupplierOrderMapper.xml:380`(guarded UPDATE).
- 대기주문 수락: `src/main/java/com/henheang/hphsar/controller/supplier/order/SupplierOrderController.java:71`(acceptPendingOrder) → `src/main/java/com/henheang/hphsar/service/implement/SupplierOrderServiceImpl.java:274`(orderStatusService.transitionOrder) → `src/main/java/com/henheang/hphsar/service/implement/OrderStatusServiceImpl.java:52`(updateStatusIfCurrent) → `src/main/resources/mapper/OrderStatusMapper.xml:30`(guarded UPDATE).
- 준비완료 → 배송 → 구매자 수령확인(공급자 권한 종료): `src/main/java/com/henheang/hphsar/controller/supplier/order/SupplierOrderController.java:107`(finishPreparing) → `src/main/java/com/henheang/hphsar/service/implement/SupplierOrderServiceImpl.java:398`(PROCESSING→DISPATCHED) → `src/main/java/com/henheang/hphsar/controller/supplier/order/SupplierOrderController.java:132`(orderDelivered) → `src/main/java/com/henheang/hphsar/service/implement/SupplierOrderServiceImpl.java:424`(항상 ForbiddenException — 수령확인은 구매자 전용).

## 관련
- [[common]]
- [[domains/marketplace]]
