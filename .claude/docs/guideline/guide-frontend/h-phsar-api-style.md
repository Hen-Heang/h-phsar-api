# h-phsar-api 프론트 스타일 가이드 (서버렌더)

> **범위**: 이 문서는 h-phsar-api 저장소에 내장된 `/web/**` Thymeleaf 대시보드(보조/내부용)만 다룬다. H-Phsar의 주 제품 프론트엔드는 별도 저장소 `h-phsar-ui`(Next.js/React, 자체 harness·도메인 문서 보유)이며 이 가이드의 범위 밖이다.

대표 샘플: `templates/supplier/products.html` + `templates/fragments/layout.html` + `static/js/app.js`.

## 마크업 구조

- 모든 화면은 `head`(공통 `<head>`) → `sidebar`/`buyer-sidebar`(역할별 좌측 메뉴) → `main-wrapper > topbar + page-content` → `scripts`(공통 JS include) 순서로 Thymeleaf fragment를 조합한다.
- `<title>`은 `"<페이지명> | H-Phsar"` 형식.
- 역할 분리는 URL 프리픽스(`/web/suppliers/**`, `/web/buyers/**`)와 사이드바 fragment 선택(`sidebar` vs `buyer-sidebar`)으로 이뤄진다.

## 클래스 명명

- 카드형 컨테이너: `card`, `card-header-custom`
- 버튼: Bootstrap 기본 클래스(`btn`, `btn-outline-secondary`) + 커스텀 강조 버튼 `btn-primary-custom`
- 검색창: `search-wrapper` > `search-input`
- 목록 total 표기: `text-muted` + inline `font-size:13px`(공통 유틸 클래스 없음 — 인라인 스타일이 흔함)

## JS 짝 규약

- 페이지 전용 로직은 각 HTML 파일 하단 `<script>`에 인라인으로 작성(별도 `.js` 파일로 분리하지 않음). 예: `openAddModal()`, `loadProducts()`, `debounceSearch()`.
- 공통 유틸(`api`, `toast`, `renderPagination`, `formatDate`, `formatCurrency`, 세션 함수)은 `static/js/app.js`에서만 정의하고 페이지 스크립트는 이를 호출만 한다 — 페이지별로 재구현하지 않는다.
- 데이터 로딩은 `api.get(path)` → `.then/await`로 받아 DOM에 직접 렌더링. 서버가 렌더링해 내려주는 목록 데이터는 없다.

## TBD

- 상태관리·컴포넌트 프레임워크 도입 계획 없음(순수 DOM 조작 유지). 코드가 SPA 프레임워크로 바뀌면 `we-init` 재실행 권장.
