# 구조 스켈레톤 (we-init 입력 — 도메인-중립)

> 결정론 스캔이 뽑은 구조 사실이다. 그룹 라벨은 관찰된 패키지 조각일 뿐 도메인 판단이 아니다.
> we-init 이 이 위에 도메인 이름·업무 흐름·룰을 입힌다. 각 항목의 `파일:줄` 은 실재 근거다.

## 모듈 지도

### 모듈: h-phsar-api  (`.`)
- 그룹 `hphsar` (20개 진입점)
  - http AdminAccountController ${base.admin.v1} — `src/main/java/com/henheang/hphsar/controller/admin/account/AdminAccountController.java:27`
    · AdminAccountController — Phase 2: ADMIN-only supplier & buyer account management. <p> Endpoints: GET /suppliers → pagina…
    - listSuppliers ${base.admin.v1}/suppliers
    - getSupplier ${base.admin.v1}/suppliers/{id}
    - updateSupplierStatus ${base.admin.v1}/suppliers/{id}/status
    - listBuyers ${base.admin.v1}/buyers
    - getBuyer ${base.admin.v1}/buyers/{id}
    - updateBuyerStatus ${base.admin.v1}/buyers/{id}/status
  - http BuyerHistoryController ${base.buyer.v1}/history — `src/main/java/com/henheang/hphsar/controller/buyer/history/BuyerHistoryController.java:24`
    - getOrderHistory ${base.buyer.v1}/history/order
    - getDraftHistory ${base.buyer.v1}/history/draft
    - deleteDraftById ${base.buyer.v1}/history/draft/{id}
    - updateDraftById ${base.buyer.v1}/history/draft/{id}
  - http BuyerNotificationController ${base.buyer.v1}/notifications — `src/main/java/com/henheang/hphsar/controller/buyer/notification/BuyerNotificationController.java:23`
    - getUserAllNotification ${base.buyer.v1}/notifications
    - markNotificationAsRead ${base.buyer.v1}/notifications/{id}/read
    - markAllNotificationAsRead ${base.buyer.v1}/notifications/read
  - http BuyerOrderController ${base.buyer.v1}/orders — `src/main/java/com/henheang/hphsar/controller/buyer/order/BuyerOrderController.java:33`
    - addProductToCart ${base.buyer.v1}/orders/cart
    - removeProductInCart ${base.buyer.v1}/orders/cart/product
    - updatePrductInCart ${base.buyer.v1}/orders/cart
    - viewAllCarts ${base.buyer.v1}/orders/carts
    - viewCartDetail ${base.buyer.v1}/orders/cart/details
    - cancelCart ${base.buyer.v1}/orders/cart/cancel
    - saveToDraft ${base.buyer.v1}/orders/cart/draft
    - confirmOrder ${base.buyer.v1}/orders/confirm
    - getOrderActivities ${base.buyer.v1}/orders
    - getOrderDetailByOrderId ${base.buyer.v1}/orders/{id}
    - markOrderAsArrived ${base.buyer.v1}/orders/{id}/arrived
    - confirmTransaction ${base.buyer.v1}/orders/{id}/receive
    - viewInvoiceByOrderId ${base.buyer.v1}/orders/invoice/{id}
    - cancelOrder ${base.buyer.v1}/orders/{id}/cancel
    - getOrderHistory ${base.buyer.v1}/orders/{id}/history
  - http BuyerProfileController ${base.buyer.v1}/profiles — `src/main/java/com/henheang/hphsar/controller/buyer/profile/BuyerProfileController.java:24`
    - createBuyerProfile ${base.buyer.v1}/profiles
    - getBuyerProfile ${base.buyer.v1}/profiles
    - updateBuyerProfile ${base.buyer.v1}/profiles
  - http BuyerReportController ${base.buyer.v1}/reports — `src/main/java/com/henheang/hphsar/controller/buyer/report/BuyerReportController.java:22`
    - getBuyerMonthlyReport ${base.buyer.v1}/reports
  - http BuyerStoreController ${base.buyer.v1}/stores — `src/main/java/com/henheang/hphsar/controller/buyer/store/BuyerStoreController.java:29`
    - getAllStore ${base.buyer.v1}/stores
    - getAllUserStoreSortByDate ${base.buyer.v1}/stores/sort/date
    - getAllUserStoreSortByCurrentUserFavorite ${base.buyer.v1}/stores/sort/favorite
    - getAllBookmarkedStore ${base.buyer.v1}/stores/bookmark
    - searchStoreByName ${base.buyer.v1}/stores/name/search
    - getAllUserStoreSortByRatedStar ${base.buyer.v1}/stores/sort/rated
    - getAllUserStoreSortByName ${base.buyer.v1}/stores/sort/name
    - getStoreById ${base.buyer.v1}/stores/{id}
    - bookmarkStoreById ${base.buyer.v1}/stores/{storeId}/bookmark
    - removeBookmarkStoreById ${base.buyer.v1}/stores/{storeId}/bookmark/remove
    - ratingStoreById ${base.buyer.v1}/stores/{storeId}/rating
    - getRatingByStoreId ${base.buyer.v1}/stores/{storeId}/rating
    - editRatingByStoreId ${base.buyer.v1}/stores/{storeId}/rating
    - deleteRatingByStoreId ${base.buyer.v1}/stores/{storeId}/rating
    - getProductListingByStoreId ${base.buyer.v1}/stores/{storeId}/products
    - getCategoryListingByStoreId ${base.buyer.v1}/stores/{storeId}/category
    - getStoreProductByCategory ${base.buyer.v1}/stores/{storeId}/products/category
    - getStoresByCategorySearch ${base.buyer.v1}/stores/category/search
    - getStoresBySearch ${base.buyer.v1}/stores/hybrid/search
  - http FileController /api/v1/files — `src/main/java/com/henheang/hphsar/controller/FileController.java:19`
    - uploadFile /api/v1/files/upload
    - getFile /api/v1/files/{id}
  - http JwtAuthenticationController /authorization — `src/main/java/com/henheang/hphsar/controller/JwtAuthenticationController.java:44`
    · JwtAuthenticationController — Authentication Endpoints <p> All endpoints here are PUBLIC (no token required). Configured…
    - insertUser /authorization/register
    - createAuthenticationToken /authorization/login
    - refreshAccessToken /authorization/refresh
    - logout /authorization/logout
    - changePassword /authorization/change-password
    - forgetPassword /authorization/forget
  - http OTPController /authorization/api/v1/otp — `src/main/java/com/henheang/hphsar/controller/otp/OTPController.java:26`
    · OTPController — OTP Endpoints <p> All endpoints here are PUBLIC (no token required). Configured in SecurityConfig → .req…
    - generateOtp /authorization/api/v1/otp/generate
    - activateAccount /authorization/api/v1/otp/verify
  - http CategoryController ${base.supplier.v1}/categories — `src/main/java/com/henheang/hphsar/controller/supplier/category/CategoryController.java:27`
    - getAllCategory ${base.supplier.v1}/categories
    - getCategoryById ${base.supplier.v1}/categories/{id}
    - editCategory ${base.supplier.v1}/categories/{id}
    - deleteCategory ${base.supplier.v1}/categories/{id}
    - createCategoryStore ${base.supplier.v1}/categories
    - searchCategoryByName ${base.supplier.v1}/categories/search
  - http SupplierHistoryController ${base.supplier.v1}/history — `src/main/java/com/henheang/hphsar/controller/supplier/history/SupplierHistoryController.java:22`
    - getProductImportHistory ${base.supplier.v1}/history/import
    - getOrderHistory ${base.supplier.v1}/history/order
  - http SupplierHomepageController ${base.supplier.v1}/order_activities — `src/main/java/com/henheang/hphsar/controller/supplier/homepage/SupplierHomepageController.java:19`
    - getNewOrder ${base.supplier.v1}/order_activities
    - months ${base.supplier.v1}/order_activities/months
  - http SupplierNotificationController ${base.supplier.v1}/notifications — `src/main/java/com/henheang/hphsar/controller/supplier/notification/SupplierNotificationController.java:19`
    - getUserAllNotification ${base.supplier.v1}/notifications
    - markNotificationAsRead ${base.supplier.v1}/notifications/{id}/read
    - markAllNotificationAsRead ${base.supplier.v1}/notifications/read
  - http SupplierOrderController ${base.supplier.v1}/orders — `src/main/java/com/henheang/hphsar/controller/supplier/order/SupplierOrderController.java:20`
    - getOrderDetailsByOrderId ${base.supplier.v1}/orders/{id}/details
    - getInvoiceByOrderId ${base.supplier.v1}/orders/invoice/{orderId}
    - getAllOrderCurrentUserSortByCreatedDate ${base.supplier.v1}/orders
    - getNewOrderCurrentUserSortByCreatedDate ${base.supplier.v1}/orders/pending
    - acceptPendingOrder ${base.supplier.v1}/orders/pending/accept/{orderId}
    - declinePendingOrder ${base.supplier.v1}/orders/pending/decline/{orderId}
    - getPreparingOrderCurrentUserSortByCreatedDate ${base.supplier.v1}/orders/preparing
    - finishPreparing ${base.supplier.v1}/orders/preparing/{orderId}
    - getDispatchingOrderCurrentUserSortByCreatedDate ${base.supplier.v1}/orders/dispatching
    - orderDelivered ${base.supplier.v1}/orders/dispatching/{orderId}
    - getConfirmingOrderCurrentUserSortByCreatedDate ${base.supplier.v1}/orders/confirming
    - getCompleteOrderCurrentUserSortByCreatedDate ${base.supplier.v1}/orders/complete
    - getOrderHistory ${base.supplier.v1}/orders/{orderId}/history
  - http SupplierProductController ${base.supplier.v1}/products — `src/main/java/com/henheang/hphsar/controller/supplier/product/SupplierProductController.java:31`
    - insertNewProduct ${base.supplier.v1}/products
    - getProductById ${base.supplier.v1}/products/{id}
    - editProduct ${base.supplier.v1}/products/{id}
    - deleteProductById ${base.supplier.v1}/products/{id}
    - getAllProductByName ${base.supplier.v1}/products/search
    - unPublishProduct ${base.supplier.v1}/products/{id}/unlist
    - publishProduct ${base.supplier.v1}/products/{id}/publish
    - getAllProductWithSorting ${base.supplier.v1}/products/sort
    - importProduct ${base.supplier.v1}/products/import
  - http SupplierProfileController ${base.supplier.v1}/profiles — `src/main/java/com/henheang/hphsar/controller/supplier/profile/SupplierProfileController.java:24`
    - getUserProfileById ${base.supplier.v1}/profiles
    - addUserProfile ${base.supplier.v1}/profiles
    - updateUserProfile ${base.supplier.v1}/profiles
  - http SupplierReportController ${base.supplier.v1}/reports — `src/main/java/com/henheang/hphsar/controller/supplier/report/SupplierReportController.java:22`
    - getSupplierReport ${base.supplier.v1}/reports
  - http SupplierStoreController ${base.supplier.v1}/stores — `src/main/java/com/henheang/hphsar/controller/supplier/store/SupplierStoreController.java:21`
    - createStore ${base.supplier.v1}/stores
    - getUserStore ${base.supplier.v1}/stores/user
    - editAllFieldUserStore ${base.supplier.v1}/stores
    - deleteUserStore ${base.supplier.v1}/stores
    - disableStore ${base.supplier.v1}/stores/disable
    - enableStore ${base.supplier.v1}/stores/enable
  - http WebViewController /web — `src/main/java/com/henheang/hphsar/controller/web/WebViewController.java:9`
    - login /web
    - register /web/register
    - supplierDashboard /web/suppliers/dashboard
    - supplierProducts /web/suppliers/products
    - supplierOrders /web/suppliers/orders
    - supplierCategories /web/suppliers/categories
    - supplierStore /web/suppliers/store
    - supplierProfile /web/suppliers/profile
    - buyerDashboard /web/buyers/dashboard
    - buyerStores /web/buyers/stores
    - buyerCart /web/buyers/cart
    - buyerOrders /web/buyers/orders
    - buyerHistory /web/buyers/history
    - buyerProfile /web/buyers/profile

## 호출 흐름 (얕은 정적 근사)

### AdminAccountController ${base.admin.v1} (http)
- 경로: AdminAccountController → AdminAccountService → AdminAccountServiceImpl → AdminAccountRepository
- 종단 테이블: TB_BUYER_ACCOUNT, TB_BUYER_INFO, TB_SUPPLIER_ACCOUNT, TB_SUPPLIER_INFO
- 근거: `src/main/java/com/henheang/hphsar/controller/admin/account/AdminAccountController.java:29` `src/main/java/com/henheang/hphsar/service/implement/AdminAccountServiceImpl.java:17` `src/main/java/com/henheang/hphsar/service/implement/AdminAccountServiceImpl.java:19`

### BuyerHistoryController ${base.buyer.v1}/history (http)
- 경로: BuyerHistoryController → HistoryService → HistoryServiceImplV1 → HistoryRepository
- 종단 테이블: TB_CATEGORY, TB_ORDER, TB_ORDER_DETAIL, TB_PRODUCT, TB_PRODUCT_IMPORT, TB_PRODUCT_IMPORT_DETAIL, TB_STORE_PRODUCT_DETAIL
- 근거: `src/main/java/com/henheang/hphsar/controller/buyer/history/BuyerHistoryController.java:25` `src/main/java/com/henheang/hphsar/service/implement/HistoryServiceImplV1.java:31` `src/main/java/com/henheang/hphsar/service/implement/HistoryServiceImplV1.java:32`

### BuyerNotificationController ${base.buyer.v1}/notifications (http)
- 경로: BuyerNotificationController → NotificationService → NotificationServiceImplV1 → StoreRepository
- 종단 테이블: TB_BOOKMARK, TB_CATEGORY, TB_ORDER, TB_ORDER_DETAIL, TB_PRODUCT, TB_RATING_DETAIL, TB_ROLE, TB_STORE, TB_STORE_CATEGORY, TB_STORE_PHONE, TB_STORE_PRODUCT_DETAIL, TB_SUPPLIER_ACCOUNT
- 근거: `src/main/java/com/henheang/hphsar/controller/buyer/notification/BuyerNotificationController.java:24` `src/main/java/com/henheang/hphsar/service/implement/NotificationServiceImplV1.java:23` `src/main/java/com/henheang/hphsar/service/implement/NotificationServiceImplV1.java:24`

### BuyerOrderController ${base.buyer.v1}/orders (http)
- 경로: BuyerOrderController → BuyerOrderService → BuyerOrderServiceImpl → NotificationRepository
- 종단 테이블: TB_BUYER_NOTIFICATION, TB_NOTIFICATION_TYPE, TB_SUPPLIER_NOTIFICATION
- 근거: `src/main/java/com/henheang/hphsar/controller/buyer/order/BuyerOrderController.java:35` `src/main/java/com/henheang/hphsar/service/implement/BuyerOrderServiceImpl.java:34` `src/main/java/com/henheang/hphsar/service/implement/BuyerOrderServiceImpl.java:35`

### BuyerProfileController ${base.buyer.v1}/profiles (http)
- 경로: BuyerProfileController → BuyerProfileService → BuyerProfileServiceImpl → BuyerProfileRepository
- 종단 테이블: TB_BUYER_ACCOUNT, TB_BUYER_INFO, TB_BUYER_PHONE
- 근거: `src/main/java/com/henheang/hphsar/controller/buyer/profile/BuyerProfileController.java:26` `src/main/java/com/henheang/hphsar/service/implement/BuyerProfileServiceImpl.java:25` `src/main/java/com/henheang/hphsar/service/implement/BuyerProfileServiceImpl.java:26`

### BuyerReportController ${base.buyer.v1}/reports (http)
- 경로: BuyerReportController → BuyerReportService → BuyerReportServiceImpl → BuyerReportRepository
- 종단 테이블: CREATED_DATE, O, TB_CATEGORY, TB_ORDER, TB_ORDER_DETAIL, TB_RATING_DETAIL, TB_STORE_PRODUCT_DETAIL
- 근거: `src/main/java/com/henheang/hphsar/controller/buyer/report/BuyerReportController.java:25` `src/main/java/com/henheang/hphsar/service/implement/BuyerReportServiceImpl.java:23` `src/main/java/com/henheang/hphsar/service/implement/BuyerReportServiceImpl.java:24`

### BuyerStoreController ${base.buyer.v1}/stores (http)
- 경로: BuyerStoreController → BuyerStoreService → BuyerStoreServiceImpl → StoreRepository
- 종단 테이블: TB_BOOKMARK, TB_CATEGORY, TB_ORDER, TB_ORDER_DETAIL, TB_PRODUCT, TB_RATING_DETAIL, TB_ROLE, TB_STORE, TB_STORE_CATEGORY, TB_STORE_PHONE, TB_STORE_PRODUCT_DETAIL, TB_SUPPLIER_ACCOUNT
- 근거: `src/main/java/com/henheang/hphsar/controller/buyer/store/BuyerStoreController.java:30` `src/main/java/com/henheang/hphsar/service/implement/BuyerStoreServiceImpl.java:24` `src/main/java/com/henheang/hphsar/service/implement/BuyerStoreServiceImpl.java:25`

### FileController /api/v1/files (http)
- 경로: FileController → FileService → FileServiceImpl → FileRepository
- 근거: `src/main/java/com/henheang/hphsar/controller/FileController.java:21` `src/main/java/com/henheang/hphsar/service/implement/FileServiceImpl.java:16` `src/main/java/com/henheang/hphsar/service/implement/FileServiceImpl.java:18`

### JwtAuthenticationController /authorization (http)
- 경로: JwtAuthenticationController → JwtTokenUtil
- 근거: `src/main/java/com/henheang/hphsar/controller/JwtAuthenticationController.java:55`

### OTPController /authorization/api/v1/otp (http)
- 경로: OTPController → OtpService → OtpServiceImplV1 → OtpRepository
- 종단 테이블: TB_BUYER_ACCOUNT, TB_BUYER_OTP, TB_ROLE, TB_SUPPLIER_ACCOUNT, TB_SUPPLIER_OTP
- 근거: `src/main/java/com/henheang/hphsar/controller/otp/OTPController.java:28` `src/main/java/com/henheang/hphsar/service/implement/OtpServiceImplV1.java:39` `src/main/java/com/henheang/hphsar/service/implement/OtpServiceImplV1.java:41`

### CategoryController ${base.supplier.v1}/categories (http)
- 경로: CategoryController → CategoryService → CategoryServiceImplV1 → CategoryRepository
- 종단 테이블: TB_CATEGORY, TB_STORE, TB_STORE_CATEGORY, TB_STORE_PRODUCT_DETAIL
- 근거: `src/main/java/com/henheang/hphsar/controller/supplier/category/CategoryController.java:28` `src/main/java/com/henheang/hphsar/service/implement/CategoryServiceImplV1.java:25` `src/main/java/com/henheang/hphsar/service/implement/CategoryServiceImplV1.java:35`

### SupplierHistoryController ${base.supplier.v1}/history (http)
- 경로: SupplierHistoryController → HistoryService → HistoryServiceImplV1 → HistoryRepository
- 종단 테이블: TB_CATEGORY, TB_ORDER, TB_ORDER_DETAIL, TB_PRODUCT, TB_PRODUCT_IMPORT, TB_PRODUCT_IMPORT_DETAIL, TB_STORE_PRODUCT_DETAIL
- 근거: `src/main/java/com/henheang/hphsar/controller/supplier/history/SupplierHistoryController.java:23` `src/main/java/com/henheang/hphsar/service/implement/HistoryServiceImplV1.java:31` `src/main/java/com/henheang/hphsar/service/implement/HistoryServiceImplV1.java:32`

### SupplierHomepageController ${base.supplier.v1}/order_activities (http)
- 경로: SupplierHomepageController → SupplierHomepageService → SupplierHomepageServiceImpl → SupplierHomepageRepository
- 종단 테이블: CREATED_DATE, TB_ORDER, TB_ORDER_DETAIL, TB_PRODUCT_IMPORT, TB_PRODUCT_IMPORT_DETAIL, TB_STORE
- 근거: `src/main/java/com/henheang/hphsar/controller/supplier/homepage/SupplierHomepageController.java:20` `src/main/java/com/henheang/hphsar/service/implement/SupplierHomepageServiceImpl.java:25` `src/main/java/com/henheang/hphsar/service/implement/SupplierHomepageServiceImpl.java:27`

### SupplierNotificationController ${base.supplier.v1}/notifications (http)
- 경로: SupplierNotificationController → NotificationService → NotificationServiceImplV1 → StoreRepository
- 종단 테이블: TB_BOOKMARK, TB_CATEGORY, TB_ORDER, TB_ORDER_DETAIL, TB_PRODUCT, TB_RATING_DETAIL, TB_ROLE, TB_STORE, TB_STORE_CATEGORY, TB_STORE_PHONE, TB_STORE_PRODUCT_DETAIL, TB_SUPPLIER_ACCOUNT
- 근거: `src/main/java/com/henheang/hphsar/controller/supplier/notification/SupplierNotificationController.java:20` `src/main/java/com/henheang/hphsar/service/implement/NotificationServiceImplV1.java:23` `src/main/java/com/henheang/hphsar/service/implement/NotificationServiceImplV1.java:24`

### SupplierOrderController ${base.supplier.v1}/orders (http)
- 경로: SupplierOrderController → SupplierOrderService → SupplierOrderServiceImpl → SupplierOrderRepository
- 종단 테이블: TB_ORDER, TB_ORDER_DETAIL, TB_PRODUCT, TB_STATUS, TB_STORE, TB_STORE_PRODUCT_DETAIL
- 근거: `src/main/java/com/henheang/hphsar/controller/supplier/order/SupplierOrderController.java:22` `src/main/java/com/henheang/hphsar/service/implement/SupplierOrderServiceImpl.java:38` `src/main/java/com/henheang/hphsar/service/implement/SupplierOrderServiceImpl.java:39`

### SupplierProductController ${base.supplier.v1}/products (http)
- 경로: SupplierProductController → SupplierProductService → SupplierProductServiceImpl → SupplierProductRepository
- 종단 테이블: TB_CATEGORY, TB_ORDER, TB_ORDER_DETAIL, TB_PRODUCT, TB_PRODUCT_CATEGORY, TB_PRODUCT_IMPORT, TB_PRODUCT_IMPORT_DETAIL, TB_STORE, TB_STORE_PRODUCT_DETAIL
- 근거: `src/main/java/com/henheang/hphsar/controller/supplier/product/SupplierProductController.java:33` `src/main/java/com/henheang/hphsar/service/implement/SupplierProductServiceImpl.java:33` `src/main/java/com/henheang/hphsar/service/implement/SupplierProductServiceImpl.java:35`

### SupplierProfileController ${base.supplier.v1}/profiles (http)
- 경로: SupplierProfileController → SupplierProfileService → SupplierProfileServiceImpl → SupplierProfileRepository
- 종단 테이블: TB_STORE, TB_SUPPLIER_INFO, TB_SUPPLIER_PHONE
- 근거: `src/main/java/com/henheang/hphsar/controller/supplier/profile/SupplierProfileController.java:26` `src/main/java/com/henheang/hphsar/service/implement/SupplierProfileServiceImpl.java:25` `src/main/java/com/henheang/hphsar/service/implement/SupplierProfileServiceImpl.java:27`

### SupplierReportController ${base.supplier.v1}/reports (http)
- 경로: SupplierReportController → SupplierReportService → SupplierReportServiceImpl → SupplierReportRepository
- 종단 테이블: TB_ORDER, TB_ORDER_DETAIL, TB_PRODUCT_IMPORT, TB_PRODUCT_IMPORT_DETAIL
- 근거: `src/main/java/com/henheang/hphsar/controller/supplier/report/SupplierReportController.java:24` `src/main/java/com/henheang/hphsar/service/implement/SupplierReportServiceImpl.java:27` `src/main/java/com/henheang/hphsar/service/implement/SupplierReportServiceImpl.java:32`

### SupplierStoreController ${base.supplier.v1}/stores (http)
- 경로: SupplierStoreController → SupplierStoreService → SupplierStoreServiceImpl → StoreRepository
- 종단 테이블: TB_BOOKMARK, TB_CATEGORY, TB_ORDER, TB_ORDER_DETAIL, TB_PRODUCT, TB_RATING_DETAIL, TB_ROLE, TB_STORE, TB_STORE_CATEGORY, TB_STORE_PHONE, TB_STORE_PRODUCT_DETAIL, TB_SUPPLIER_ACCOUNT
- 근거: `src/main/java/com/henheang/hphsar/controller/supplier/store/SupplierStoreController.java:23` `src/main/java/com/henheang/hphsar/service/implement/SupplierStoreServiceImpl.java:21` `src/main/java/com/henheang/hphsar/service/implement/SupplierStoreServiceImpl.java:23`

### WebViewController /web (http)
- 경로: WebViewController

## 관찰 용어 (project-meta 시드)

- 패키지 그룹: hphsar
- 테이블: CREATED_DATE, O, TB_ADMIN_ACCOUNT, TB_BOOKMARK, TB_BUYER_ACCOUNT, TB_BUYER_INFO, TB_BUYER_NOTIFICATION, TB_BUYER_OTP, TB_BUYER_PHONE, TB_CATEGORY, TB_NOTIFICATION_TYPE, TB_ORDER, TB_ORDER_DETAIL, TB_ORDER_STATUS_HISTORY, TB_PRODUCT, TB_PRODUCT_CATEGORY, TB_PRODUCT_IMPORT, TB_PRODUCT_IMPORT_DETAIL, TB_RATING_DETAIL, TB_REFRESH_TOKEN, TB_ROLE, TB_STATUS, TB_STORE, TB_STORE_CATEGORY, TB_STORE_PHONE, TB_STORE_PRODUCT_DETAIL, TB_SUPPLIER_ACCOUNT, TB_SUPPLIER_INFO, TB_SUPPLIER_NOTIFICATION, TB_SUPPLIER_OTP, TB_SUPPLIER_PHONE
- 상수(상위 50): Boolean.FALSE, Boolean.TRUE, ChronoField.NANO_OF_SECOND, ChronoUnit.MONTHS, Code.DELIVERY_ACCEPTED, Code.FETCHED, Code.NOTIFICATION_FETCHED, Code.PROFILE_CREATED, Code.PROFILE_FETCHED, Code.PROFILE_UPDATED, Code.SUCCESS, DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, ExceptionMessages.ADDITIONAL_PHONE_FORMAT_INVALID, ExceptionMessages.ADDITIONAL_PHONE_PREFIX_INVALID, ExceptionMessages.BUYER_NOT_FOUND, ExceptionMessages.CAN_NOT_USE_DEFAULT_VALUE_PLEASE_INPUT_LEGAL, ExceptionMessages.CAN_NOT_USE_DEFAULT_VALUE_PLEASE_INPUT_VALUE, ExceptionMessages.CART_DOES_NOT_EXIST, ExceptionMessages.DESCRIPTION_WORD_COUNT_CAN_NOT_EXCEED_100, ExceptionMessages.DRAFT_NOT_FOUND, ExceptionMessages.FAIL_TO_CHANGE_STATUS, ExceptionMessages.FAIL_TO_CREATE_NOTIFICATION, ExceptionMessages.FAIL_TO_FETCH_ORDER_DETAILS, ExceptionMessages.FAIL_TO_FETCH_ORDER_INVOICE, ExceptionMessages.FIELD_SORT_IS_INVALID_PLEASE_INPUT_EITHER_ASC, ExceptionMessages.INACTIVE_IS_REQUIRED, ExceptionMessages.INTEGER_OVERFLOW, ExceptionMessages.INVALID_INPUT_AVAILABLE_SORTING_ARE_CREATED_DATE_IS, ExceptionMessages.NOTIFICATION_NOT_FOUND, ExceptionMessages.NOT_ENOUGH_PRODUCT_IN_STOCK, ExceptionMessages.NO_CART_IS_FOUND, ExceptionMessages.ONE_OF_THE_FIELDS_INSIDE_THE_STOREREQUEST_OBJECT, ExceptionMessages.ORDER_NOT_FOUND, ExceptionMessages.OUT_OF_RANGE_RATING_RANGE_IS_FROM_1, ExceptionMessages.PAGE_SIZE_MUST_BE_POSITIVE, ExceptionMessages.PRIMARY_PHONE_FORMAT_INVALID, ExceptionMessages.PRODUCTS_NOT_FOUND, ExceptionMessages.SOMETHING_WENT_WRONG_WHILE_DELETING, ExceptionMessages.SOMETHING_WENT_WRONG_WHILE_DOING_BOOKMARK_OPERATION, ExceptionMessages.STORES_NOT_FOUND, ExceptionMessages.STORE_NOT_FOUND, ExceptionMessages.SUPPLIER_NOT_FOUND, ExceptionMessages.THERE_IS_NO_ORDER_CURRENTLY, ExceptionMessages.THIS_NOTIFICATION_DOES_NOT_EXIST, ExceptionMessages.THIS_ORDER_IS_NOT_PENDING, ExceptionMessages.THIS_PRODUCT_IS_NOT_FOUND_IN_THIS_STORE, ExceptionMessages.THIS_STORE_ID_DOES_NOT_EXIST, ExceptionMessages.THIS_USER_DOES_NOT_EXIST, ExceptionMessages.USER_HAVE_NOT_CREATED_PROFILE_YET_PLEASE_CREATE, ExceptionMessages.USER_HAVE_NOT_CREATED_STORE
- 민감 토큰: (없음)
