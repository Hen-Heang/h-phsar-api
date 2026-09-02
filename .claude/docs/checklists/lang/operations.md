# Operations — Java 구현 예시

> Core 원칙: `docs/checklists/operations.md` 참조.
> 이 파일은 Java 언어 보편의 운영 관련 탐지 키워드 및 코드 예시를 제공한다.
> (Spring 비동기·배치·외부연동 특화는 fw-spring 의 operations.md.fwpart 가산.)

---

## OPS-01 — 배포 시 의존성·설정 동기화

**탐지 키워드:** `pom.xml`, `build.gradle`, `dependency 추가`, `공통 설정`, `라이브러리 참조`, `배포 절차`

**체크 항목:**
- 빌드 의존성 파일(`pom.xml`/`build.gradle`) 신규 dependency 추가 PR에 **배포 대상 서버 목록**과 **lib 동기화 확인 단계**가 기재되었는가?
- 빌드 산출물에 누락 시 부팅 단계에서 fail-fast 하는가? (런타임 `NoClassDefFoundError`/`ClassNotFoundException` 회피)

---

## OPS-02 — 비동기·배치·외부 연동 실패 임계치 알람 정의 (Spring 전용)

**탐지 키워드:** `@Scheduled`, `@KafkaListener`, `Job`, `Step`, `RestTemplate`, `WebClient`, `HttpClient`

**체크 항목:**
- 신규/수정 배치·데몬·외부연동 PR에 **실패 임계치 알람** 정의가 포함되었는가?
- 실패율·타임아웃 발생률·미처리 건수가 모니터링 대시보드에 등록되었는가?
- 알람은 Slack/SMS 등 적시성 채널로 에스컬레이션되는가?

**권장 임계치 예시:**
- 알림 발송 실패율 > 1% / 5분
- 배치 실패 1회 발생 즉시
- 외부 API 응답시간 P95 > 5초

**적용 영역:** `@Scheduled`, Quartz Job, Kafka Consumer, 외부 API 호출.
