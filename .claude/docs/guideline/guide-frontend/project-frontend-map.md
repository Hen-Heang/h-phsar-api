> 이 표는 h-phsar-api 저장소 **안에 내장된** 화면만 다룬다. H-Phsar의 주 제품 프론트엔드(Buyer/Supplier/Admin 전체 UI)는 별도 저장소 `h-phsar-ui`(Next.js/React)이며 여기 포함되지 않는다.

| 프로젝트 | UI 라이브러리 | JS 패턴 | 패키징 |
|---------|-------------|--------|-------|
| h-phsar-api (보조/내부용 `/web/**` 대시보드) | Thymeleaf 템플릿 + Bootstrap 5(CDN) + Bootstrap Icons | 전역 함수 스타일 바닐라 JS (`static/js/app.js`), `fetch` 기반 `api.get/post/put/delete` 헬퍼로 REST API 호출. 서버는 얇은 HTML 뼈대만 렌더링하고 실제 데이터는 페이지 로드 후 JS가 API를 호출해 채운다. | 빌드 도구 없음 — 정적 리소스를 `src/main/resources/static`·`templates`에 직접 배치, Spring Boot가 그대로 서빙 |
