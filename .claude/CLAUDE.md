# CLAUDE.md — app-workspace

> 워크스페이스 메타·인벤토리·가이드 매핑: `.claude/config/project.yaml`. 도메인 키워드: `.claude/config/project-meta.yaml`. 툴 동작·gitlab·scope 토글: `.claude/config/system.yaml`. 스코프 데이터(scope 기능 사용 시): `.claude/config/scope.yaml`.

이 워크스페이스는 H-Phsar의 B2B 도매 마켓플레이스 백엔드입니다.

- 프로젝트 1개 (h-phsar-api) — 단일 Spring Boot 애플리케이션
- 기술 스택: Java 17 / Spring Boot 3.2.5 / MyBatis(XML 매퍼) / PostgreSQL
- 역할별 접근: SUPPLIER(공급자) / BUYER(구매자) / ADMIN(관리자)
- REST API(`/api/v1/suppliers`, `/api/v1/buyers`)와 서버렌더 대시보드(`/web/**`, Thymeleaf)를 한 애플리케이션이 함께 제공합니다.
- **주 제품 프론트엔드는 이 저장소가 아니라 별도 저장소 `h-phsar-ui`(Next.js/React, 자체 harness 보유)입니다.** 이 저장소의 `/web/**` Thymeleaf 화면은 실제로 동작하는 보조/내부용 대시보드이며, "메인 프론트엔드"로 오인하지 않습니다.

## 이 프로젝트의 관행 (감지 기반)

**명명**: 패키지는 `com.henheang.hphsar.{layer}.{role}...` 구조(예: `controller.buyer.order`, `service.implement`). 컨트롤러는 `*Controller`, 서비스는 `*Service`(+구현체 `*ServiceImpl`), 리포지토리 인터페이스는 `*Repository`(SQL은 동명의 `*Mapper.xml`).

**계층**: Controller → Service → Repository(인터페이스) → Mapper XML → PostgreSQL. 모든 컨트롤러는 `BaseController`를 상속해 `ok(...)` 헬퍼로 `ApiResponse`/`PagedResponse`를 감싸 반환합니다.

**응답 계약**: 성공은 `common.api.ApiResponse<T>`/`PagedResponse<T>`, 실패는 `ApiErrorResponse` + 전역 예외 핸들러 + `Code`(애플리케이션 코드-HTTP 상태 매핑) 조합을 씁니다.

**MyBatis**: `#{}` 파라미터 바인딩이 표준입니다. `SupplierReportMapper.xml`의 날짜 범위 조회처럼 `${startDate}/${endDate}` 문자열 치환이 남아있는 레거시 구간이 있어 사용자 입력이 그 경로로 들어가지 않는지 주의가 필요합니다. 재고 차감·주문 상태 전이는 "가드된 UPDATE + 영향 행 수 확인" 패턴(`OrderStatusRepository.updateStatusIfCurrent` 등)을 씁니다.

**테스트**: 단위 테스트는 `*Test`(JUnit 5 + Mockito, `@ExtendWith(MockitoExtension.class)`), Mapper·동시성 등 DB 동작 검증은 `*IT`(PostgreSQL Testcontainers, `AbstractIntegrationTest` 상속)로 분리합니다.

**주문 상태**: `CART → DRAFT → PENDING → PROCESSING → DISPATCHED → COMPLETED`(또는 `REJECTED`/`CANCELLED`)이며, 숫자 status id를 Java 코드에 두지 않고 이름 기반으로 `tb_status`를 조회해 전이합니다(자세한 내용은 `docs/ORDER_WORKFLOW.md`).

**핵심 용어**: 공급자(Supplier), 구매자(Buyer), 관리자(Admin), 스토어(Store), 상품(Product), 주문/장바구니(Order/Cart), 재고(Stock, `tb_store_product_detail`), 알림(Notification), 리포트(Report), OTP 인증.

이 프로젝트의 도메인 페이지: `.claude/docs/domain/modules/h-phsar-api.md`. 공통 관행: `.claude/docs/domain/common.md`. 전체 색인: `.claude/docs/domain/index.md`.
---

## 산출물 경로 규칙

- **`target/` 디렉토리는 항상 워크스페이스 루트(이 `CLAUDE.md`가 위치한 디렉토리)를 기준으로 생성한다.**
- `/develop`로 스코프가 설정되어 있더라도 프로젝트 하위가 아닌 **워크스페이스 루트 아래** `target/`에 생성한다.
- 워크스페이스 루트 디렉토리명은 개발자마다 다를 수 있으므로, `.claude/` 디렉토리의 부모 경로로 결정한다.
- 예시: `{워크스페이스 루트}/{{config.outputDir}}/plans/`, `{워크스페이스 루트}/{{config.outputDir}}/`, `{워크스페이스 루트}/target/optimized/`

---

## 세션 시작 규칙

- HANDOFF 는 **2개 자산**으로 관리된다 (둘 다 워크스페이스 루트):
  - `HANDOFF.md` — 진행중 컨텍스트 (모든 프로젝트의 Plan / Next / Caution / Files 를 프로젝트별 섹션으로 통합 + Cross-Cutting Caution + Common Files)
  - `HANDOFF_HISTORY.md` — 완료 작업 + 진행중 스냅샷 누적 이력 (세션 단위, 시간 역순 prepend)
- **프로젝트별 `{project}/HANDOFF.md` 는 작성하지 않는다.** 모든 진행중 컨텍스트는 루트 `HANDOFF.md` 안에 통합된다.
- 세션 시작 시:
  1. 루트 `HANDOFF.md` 가 존재하면 **반드시 읽는다** (진행중 컨텍스트 — 이것만으로 세션 재개에 충분하다)
  2. 루트 `HANDOFF_HISTORY.md` 는 **기본적으로 읽지 않는다.** HANDOFF.md 만으로 맥락이 부족할 때만 **최신 1개 entry** 를 본다 (`awk '/^## /{c++} c>1{exit} {print}' HANDOFF_HISTORY.md`) — 그 너머는 토큰 낭비, 필요 시 아래 "이력 조회" awk 로 특정 entry 만 조회
- **브랜치 정합성 인지**: `HANDOFF.md` frontmatter `projects:` 의 각 항목 브랜치가 해당 프로젝트의 현재 git 브랜치와 다르면 그 프로젝트 섹션은 **stale 컨텍스트로 인지**한다 (정보용으로만 활용, Next 항목 행동은 `/develop` 호출 후 5.5단계 surface 를 거쳐 진행).
- **stale 섹션 자동 보존은 `/pack` 0단계(P2)가 담당**: 다음 pack 호출 시 stale 프로젝트 섹션의 진행중 컨텍스트는 `HANDOFF_HISTORY.md` 에 자동 prepend 되고 HANDOFF.md 의 해당 섹션은 비워진다 (사용자 모달 없음, 데이터 손실 없음).
- **이력 조회** (각 entry 끝에 `---` 종결 마커 있음, awk 사용):
  - 단일 entry (가장 최근): `awk '/— {project} @ {branch}$/{f=1} f; f && /^---$/{exit}' HANDOFF_HISTORY.md`
  - 브랜치별 모든 entry: `awk '/@ {branch}$/,/^---$/' HANDOFF_HISTORY.md`

---

## 프로젝트 인벤토리

> **단일 출처**: `.claude/config/project.yaml` `projects[]` — 프로젝트명·shortName·multiModule·role·buildArgs·guideline 모두 본 파일이 마스터.
>
> 각 프로젝트의 상세 역할은 프로젝트 루트의 `CLAUDE.md` 도입부 참조.
>
> 신규 프로젝트 등록·소멸 프로젝트 정리·항목 변경은 `/config-update` 로 동기화한다 — 인스턴스 정본(`.we-adp/config/`)과 `.claude/` 산출물을 함께 갱신하므로 `we-adp update` 에 유실되지 않는다. `.claude/config/project.yaml` 을 손으로 고치지 않는다 (본 CLAUDE.md 동기화도 불필요).
