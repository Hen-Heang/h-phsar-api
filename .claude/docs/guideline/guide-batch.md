# 개발 가이드 — Batch

> **적용 대상:** 정기 배치(Job) 프로젝트
> **유형:** Spring Boot 기반 야간/정기 배치 서비스
> **단일 출처:** `.claude/config/project.yaml` `projects[]` (guideline.backend = `guide-batch.md`)
>
> 본 가이드는 워크스페이스 고유 어노테이션을 사용한다:
> - `{{config.batch.jobAnnotation}}` — Job 마커
> - `{{config.mapper.primaryAnnotation}}` — 주 데이터소스 Mapper
> - `{{config.mapper.secondaryAnnotation}}` — 보조 데이터소스 Mapper
>
> 어노테이션 명·도입 모듈은 본 워크스페이스(`annotations/` 패키지) 정의 단일 출처.

---

## 1. 패키지 구조

기본 패키지는 `{{config.baseNamespacePattern}}.batch` 이며, Job 단위로 패키지를 구성한다.

```
{{config.baseNamespacePattern}}.batch
  ├── BatchApplication.java
  ├── BatchRunner.java            ← ApplicationRunner 구현체, Job 진입점
  ├── annotations/                ← {{config.batch.jobAnnotation}}, {{config.mapper.primaryAnnotation}}, {{config.mapper.secondaryAnnotation}}
  ├── common/
  │   ├── JobProcessor.java       ← Job 인터페이스 (run 메소드 정의)
  │   ├── ServiceProcessor.java   ← Service 기반 추상 클래스
  │   └── model/
  │       ├── BatchResult.java    ← Job 실행 결과 모델
  │       └── dto/                ← 공통 배치 DTO
  ├── config/                     ← MyBatis, DataSource 설정
  ├── constants/                  ← ResultCode, Constants
  ├── exceptions/                 ← BatchException
  └── jobs/
      └── {category}/             ← 도메인 카테고리 (card, payment 등)
          └── {jobName}/
              ├── {JobName}Job.java
              ├── service/
              │   ├── {JobName}Service.java       (인터페이스)
              │   ├── impl/
              │   │   └── {JobName}ServiceImpl.java
              │   └── factory/                    (멀티 구현체 시)
              ├── mapper/
              │   └── {JobName}Mapper.java        ({{config.mapper.primaryAnnotation}} 또는 {{config.mapper.secondaryAnnotation}})
              └── model/dto/
                  ├── {Table}{CRUD}{###}In.java
                  └── {Table}{CRUD}{###}Out.java
```

## 2. Job 클래스 구조

모든 Job 클래스는 `{{config.batch.jobAnnotation}}` 어노테이션과 `JobProcessor` 인터페이스를 구현한다.

```java
@Slf4j
{{config.batch.jobAnnotation}}(name = "cardOtcRtrvlJob")
@RequiredArgsConstructor
public class CardOtcRtrvlJob implements JobProcessor {

    private final CardOtcRtrvlService cardOtcRtrvlService;

    /**
     * 배치 실행 메소드.
     *
     * @param args 실행 인수
     * @return 배치 실행 결과 {@link BatchResult}
     */
    @Override
    public BatchResult run(String[] args) {
        LocalDateTime jobStartDt = LocalDateTime.now();
        log.info("===== {} START =====", "cardOtcRtrvlJob");
        BatchResult batchResult = cardOtcRtrvlService.process();
        log.info("===== {} END ===== duration: {}", "cardOtcRtrvlJob",
                Duration.between(jobStartDt, LocalDateTime.now()).toMillis());
        return batchResult;
    }
}
```

## 3. Mapper 인터페이스

- Batch 프로젝트는 `@Mapper` 대신 **커스텀 어노테이션**을 사용한다.
  - `{{config.mapper.primaryAnnotation}}` — 주 데이터소스 Mapper
  - `{{config.mapper.secondaryAnnotation}}` — 보조 데이터소스 Mapper
- MyBatis XML Mapper 위치: `classpath:{{config.baseNamespacePattern as path}}/batch/**/mapper/*Mapper.xml`

```java
{{config.mapper.primaryAnnotation}}
public interface OrderReprocessJobMapper {

    /**
     * 채널 정보 조회.
     *
     * @param channelId 채널 ID
     * @return 채널명
     */
    String channelInfoR001(String channelId);

    List<OrderLedgerR001Out> orderLedgerR001(String channelId);

    List<ChannelInfoR003Out> channelInfoR003(String channelExternalKey);
}
```

## 4. Mapper 메소드 명명 규칙

`{tableNameCamelCase}{C|R|U|D}{###}` 패턴을 따른다.

```
C = INSERT  : itemLedgerC001(ItemLedgerC001In)   → String
R = SELECT  : orderLedgerR001(String channelId)     → List<Out>
U = UPDATE  : userPayOtCrtHistU001()                         → int
D = DELETE  : (테이블명)D001(...)                              → int
```

- 파라미터가 없는 경우 (파라미터 없는 배치 처리) 허용
- 시퀀스/채번: `get{TableNameCamelCase}()` 패턴 유지

## 5. DTO 명명 규칙

`{TableNameCamelCase}{C|R|U|D}{###}{In|Out}` 패턴을 따른다.

| 유형    | Lombok 어노테이션                                   |
| ------- | --------------------------------------------------- |
| In DTO  | `@Getter @Builder @Alias("lowerCamelCase클래스명")` |
| Out DTO | `@Getter @Setter @Alias("lowerCamelCase클래스명")`  |

```java
@Getter
@Builder
@Alias("itemLedgerC001In")
public class ItemLedgerC001In {
    private String channelId;
    private String cardNo;
    private String vprd;
    private String rgtrId;
}
```

## 6. BatchResult 반환 모델

Job은 항상 `BatchResult` 를 반환한다.

```java
BatchResult batchResult = new BatchResult();
batchResult.setResultCode(ResultCode.SUCCESS.getCode());
batchResult.setResultMsg(ResultCode.SUCCESS.getMsg());
batchResult.setTotCnt(totalCount);
batchResult.setFailCnt(failCount);
return batchResult;
```

## 7. Factory 패턴 (멀티 구현체)

복수의 서비스 구현체가 필요한 경우 (예: 채널별 처리), Factory 패턴을 사용한다.
Factory의 routing key는 도메인 식별자(`channelId` 등)를 사용한다.

```java
@Component
@Slf4j
public class OrderReprocessFactory {
    private final Map<String, OrderReprocessService> services = new HashMap<>();

    public OrderReprocessFactory(List<OrderReprocessService> serviceList) {
        serviceList.forEach(s -> services.put(s.channel().getChannelId(), s));
    }

    public OrderReprocessService getService(final String channelId) {
        OrderReprocessService service = services.get(channelId);
        if (service == null) {
            throw new BatchException(ResultCode.ERROR_UNEXPECTED.getCode(), "...");
        }
        return service;
    }
}
```


---

## 코드리뷰 평가 (code-review)

`/code-review`·`/mr-review` 가 본 가이드 대상 변경을 리뷰할 때 적용하는 유형별 평가 항목.

<type-eval>

- `{{config.batch.jobAnnotation}}` Job 마커
- Factory routing key (멀티 구현체 분기 시)

</type-eval>
