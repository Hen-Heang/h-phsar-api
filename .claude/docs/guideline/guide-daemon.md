# 개발 가이드 — Daemon

> **적용 대상:** 상시 스케줄 데몬 프로젝트
> **유형:** Spring Boot 기반 `@EnableScheduling` + `@Scheduled` 상시 데몬 서비스
> **단일 출처:** `.claude/config/project.yaml` `projects[]` (guideline.backend = `guide-daemon.md`)
>
> 스케줄러는 `@EnableScheduling` + `@Scheduled` 기반. Mapper 는 표준 `@Mapper` 사용 (Batch 의 `{{config.mapper.primaryAnnotation}}`/`{{config.mapper.secondaryAnnotation}}` 와 다름).

---

## 1. 패키지 구조

기본 패키지는 `{{config.baseNamespacePattern}}.daemon.{module}` 이며, 멀티모듈 구조를 따른다.

```
{{config.baseNamespacePattern}}.daemon.{module}
  ├── {ModuleName}Application.java  ← @SpringBootApplication @EnableScheduling
  ├── configs/
  │   ├── ScheduleConfig.java       ← ThreadPoolTaskScheduler 설정
  │   ├── JasyptConfig.java
  │   └── SecurityConfig.java
  ├── constants/                    ← ResultCode, Constants (모듈 내 자체 정의)
  ├── controller/                   ← 헬스체크 등 관리용 엔드포인트
  ├── exception/                    ← 커스텀 예외
  ├── handler/                      ← 전역 예외 핸들러
  ├── mapper/                       ← @Mapper
  ├── model/dto/                    ← {Table}{CRUD}{###}{In|Out}
  ├── schedule/                     ← @Scheduled 스케줄러
  │   ├── {ModuleName}Scheduler.java  (인터페이스)
  │   └── impl/
  │       └── {Agency}{ModuleName}Schedular.java
  └── service/                      ← 비즈니스 로직 (직접 @Service, 인터페이스 없음)
      ├── factory/                  ← Factory (기관별 라우팅)
      └── impl/                     ← 기관별 구현체 (AlarmCommonService 등)
```

## 2. Application 진입점

`@EnableScheduling` 은 반드시 Application 클래스에 선언한다.

```java
@SpringBootApplication
@EnableScheduling
public class AlarmDaemonApplication {
    public static void main(String[] args) {
        SpringApplication.run(AlarmDaemonApplication.class, args);
    }
}
```

## 3. 스레드 풀 설정 (ScheduleConfig)

각 데몬 모듈은 독립적인 스레드 풀을 구성한다.

```java
@Configuration
public class ScheduleConfig implements SchedulingConfigurer {

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);                    // 모듈별 적절한 크기로 설정
        scheduler.setThreadNamePrefix("alarm-daemon"); // 모듈명으로 접두사 설정
        scheduler.initialize();
        taskRegistrar.setTaskScheduler(scheduler);
    }
}
```

## 4. 스케줄러 클래스

- 인터페이스로 메소드를 정의하고, 기관별/용도별 구현체를 작성한다.
- 각 `@Scheduled` 메소드는 try-catch 로 예외를 반드시 처리한다.

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultAlarmScheduler implements AlarmScheduler {

    private final AlarmTranService alarmTranService;

    @Scheduled(cron = "* * * * * *")
    @Override
    public void msgWaitToCmptn() {
        try {
            alarmTranService.moveWaitToCmptn(Constants.USAG_ID_BIZ);
        } catch (Exception e) {
            log.error("msgWaitToCmptn error :: {}", LogUtil.getStackTrace(e));
        }
    }
}
```

## 5. Service 계층 패턴

- REST API 프로젝트와 달리, **Service 인터페이스 없이 직접 `@Service` 클래스**를 작성하는 경우가 많다.
- 기관별 분기가 필요한 경우에는 공통 인터페이스를 정의하고 Factory 패턴으로 라우팅한다.

```java
// 직접 Service (인터페이스 없음) — 단일 구현체
@Service
@Slf4j
@RequiredArgsConstructor
public class AlarmTranService {
    private final AlarmTranMapper alarmTranMapper;

    @Transactional(rollbackFor = Exception.class)
    public void moveWaitToCmptn(String usagId) { ... }
}

// 기관별 분기가 필요한 경우 — 인터페이스 + 구현체 + Factory
public interface AlarmCommonService {
    String usagId();
    void sendAlarm(...);
}
```

## 6. Factory 패턴 (기관별 라우팅)

Daemon 프로젝트의 Factory는 `version()` 이 아닌 `usagId()` 등 도메인 식별자를 routing key로 사용한다.

```java
@Component
@Slf4j
public class AlarmServiceFactory {
    private static final String DEFAULT_USAG_ID = "UA00000000";
    private final Map<String, AlarmCommonService> services = new HashMap<>();

    public AlarmServiceFactory(List<AlarmCommonService> serviceList) {
        serviceList.forEach(s -> services.put(s.usagId(), s));
    }

    public AlarmCommonService getAlarmService(final String usagId) {
        return services.getOrDefault(usagId, services.get(DEFAULT_USAG_ID));
    }
}
```

## 7. Mapper 메소드 명명 규칙

`{tableNameCamelCase}{C|R|U|D}{###}` 패턴을 따른다.
`@Mapper` 어노테이션을 사용한다 (Batch 프로젝트의 `{{config.mapper.primaryAnnotation}}` 와 다름).

```java
@Mapper
public interface AlarmTranMapper {
    List<String> ntfkSndngWaitInfoR001(String usagId);
    int ntfkSndngCmptnLdgrC001(List<String> sndngIds);
    int ntfkSndngWaitInfoD001(List<String> sndngIds);
    String getMsgTrxnSn();   // 시퀀스 채번
}
```

## 8. DTO 명명 및 Lombok

`{TableNameCamelCase}{C|R|U|D}{###}{In|Out}` 패턴을 따른다.

| 유형        | Lombok 어노테이션                                            |
| ----------- | ------------------------------------------------------------ |
| In DTO      | `@Getter @Setter @ToString @Alias("lowerCamelCase클래스명")` |
| Out DTO     | `@Getter @Setter @Alias("lowerCamelCase클래스명")`           |
| 일부 In DTO | `@Data @Alias("lowerCamelCase클래스명")` (레거시 허용)       |


---

## 코드리뷰 평가 (code-review)

`/code-review`·`/mr-review` 가 본 가이드 대상 변경을 리뷰할 때 적용하는 유형별 평가 항목.

<type-eval>

- `@EnableScheduling` Application 선언
- `@Scheduled` cron 표현식 유효성
- Factory routing key (기관별 라우팅)

</type-eval>
