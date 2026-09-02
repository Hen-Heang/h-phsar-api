## h-phsar-api — 프론트 패턴 (server-rendered)

> 범위: h-phsar-api 저장소 내장 `/web/**` 보조 대시보드만. 주 제품 프론트엔드(`h-phsar-ui`, Next.js/React)는 별도 저장소·별도 harness.

- **AJAX**: 전역 `api` 객체(`static/js/app.js`) — `api.get/post/put/delete(path[, body])`. 내부적으로 `apiFetch()`가 `Authorization: Bearer <token>` 헤더를 붙이고, 401 응답이면 세션을 지우고 `/web/login`으로 리다이렉트한다.
- **인증 토큰**: JWT를 `localStorage`(`hphsar_token`/`hphsar_role`/`hphsar_userId`)에 저장. 페이지 진입 시 `redirectIfNoToken()`으로 만료 여부를 클라이언트에서 먼저 확인한다.
- **UI 컴포넌트**: Bootstrap 5 클래스 + 커스텀 클래스(`btn-primary-custom`, `card-header-custom`, `search-wrapper` 등)를 `static/css/style.css`에서 오버라이드. 공통 조각(`sidebar`, `buyer-sidebar`, `topbar`, `scripts`)은 Thymeleaf fragment(`templates/fragments/layout.html`)로 재사용.
- **데이터 렌더링**: 서버 템플릿은 얇은 뼈대만 담당하고, 목록·상세 데이터는 페이지 로드 후 JS가 `api.get(...)`으로 불러와 DOM에 직접 그린다(서버 사이드 `th:each` 데이터 반복은 거의 쓰지 않음).
- **페이지네이션/토스트**: `renderPagination()`, `toast()` 공통 헬퍼 재사용(중복 구현 금지).
