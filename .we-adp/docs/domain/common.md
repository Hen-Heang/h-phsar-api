---
title: 공통 관행
type: common
updated: 2026-09-02
---

플랫폼 전반에 반복되는 패턴(§0). 모듈이 하나뿐이라 모두 `h-phsar-api` 안에서 관찰됐지만, 여러 컨트롤러/서비스에 걸쳐 반복되므로 한 번만 적는다.

## 핵심

- [확실][표준] 상태(주문 상태·상품 발행 여부 등) 변경은 항상 "현재 값을 WHERE 조건에 건 UPDATE 1건 성공 여부"로 확정한다(사전 SELECT 비교는 참고용 빠른 실패일 뿐, 최종 판정이 아니다) — 근거 `src/main/resources/mapper/OrderStatusMapper.xml:30` "WHERE id = #{orderId} AND status_id = (SELECT id FROM tb_status WHERE name = #{currentStatus})", `src/main/resources/mapper/SupplierOrderMapper.xml:380` "WHERE id = #{storeProductId} AND qty >= #{quantity}".
- [확실][표준] 모든 컨트롤러는 `BaseController`를 상속해 `ok(...)` 헬퍼로 성공 응답을 `ApiResponse`/`PagedResponse`로 감싸 반환한다 — 근거 `src/main/java/com/henheang/hphsar/controller/BaseController.java:15` "protected <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {".
- [확실][표준] 실패는 도메인 예외(`NotFoundException`/`ConflictException`/`ForbiddenException`/`BadRequestException` 등) + 전역 예외 핸들러가 `ApiErrorResponse` + `Code`로 변환한다(서비스가 직접 에러 문자열을 만들지 않는다).
- [확실][고유] 소유권 검증은 서비스 계층에서 `EXISTS`류 쿼리로 "이 리소스가 현재 사용자(또는 그 사용자의 스토어) 것인지"를 먼저 확인한 뒤에만 조회/변경을 허용한다(IDOR 방지) — 근거 `src/main/resources/mapper/SupplierOrderMapper.xml:320` "SELECT EXISTS(SELECT * FROM tb_order WHERE id = #{orderId} AND store_id = #{storeId})".
- [확실][고유] 동적 정렬 컬럼은 화이트리스트 Map으로만 허용해 사용자 입력을 그대로 SQL 컬럼명으로 쓰지 않는다 — 근거 `src/main/java/com/henheang/hphsar/service/implement/BuyerStoreServiceImpl.java:231` "Map<String, String> validColumns = Map.of(".
- [확실][표준] 인증이 필요한 프로필/스토어/OTP 계열 리소스는 "생성돼 있어야만 수정 가능"(선행 생성 가드) 패턴을 공유한다.
- [확실][고유] 서버렌더 페이지(`templates/**`)의 JS는 `api.post(...)` 호출 시 백엔드 요청 DTO의 필드와 정확히 일치하는 바디를 보내야 한다 — 클라이언트에서 검증만 하고 서버로 안 보내는 필드가 있으면 서버 검증이 항상 실패한다(2026-09-02 register.html의 `confirmPassword` 누락 사례 참고, [[modules/h-phsar-api]] "## 핵심" 참고).

## 관련
- [[modules/h-phsar-api]]
