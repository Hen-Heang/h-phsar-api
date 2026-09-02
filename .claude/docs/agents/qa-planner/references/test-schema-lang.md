# 테스트 환경·패턴 — Java/Spring

> qa-planner가 Java 프로젝트에서 참조하는 언어팩 보강 문서.

## 테스트 환경
| 항목 | 값 |
|---|---|
| 런타임 | Java 17 |
| 단위 테스트 | JUnit 5 |
| Mock | Mockito (`@Mock`, `@InjectMocks`, `@MockBean`) |
| 웹 계층 | `@WebMvcTest` + MockMvc |
| 빌드 | {{config.build.tool}} |

## 기존 테스트 패턴 탐색 시 파악 항목
- 테스트 클래스 명명 규칙
- 어노테이션 패턴(`@WebMvcTest`, `@SpringBootTest`, `@ExtendWith`)
- Mock 패턴(`@MockBean` 등)

## 자동 측정 품질 항목
- JavaDoc 작성 여부(Checkstyle / 빌드 시점)
- Jasypt `ENC(...)` 적용(grep 패턴)

## TC 로 만들지 않는 것 — 프레임워크·라이브러리 보증 영역

Core `playbook-tc-mapping.md` §6-2.5 "상한 초과 시 제거 순서 1번"의 Java 구체 목록. 아래는 라이브러리가 이미 보증하므로 TC 를 만들지 않는다 (우리 코드가 아니라 남의 코드 검증).

| 영역 | 예시 | 대신 검증할 것 |
|---|---|---|
| Lombok 생성 코드 | `@Getter`/`@Setter`/`@Builder` 가 만든 접근자·빌더 왕복, `@ToString` 출력 | 그 값을 **쓰는 쪽**의 분기·저장 결과 |
| 애노테이션 존재 확인 | 리플렉션으로 `@Alias`·`@JsonInclude` 부착 여부 단언 | 애노테이션이 만들어내는 **관측 가능한 동작**(직렬화 결과 키 집합 등) 1개로 축약 |
| Jackson 기본 직렬화 | 필드 순서, null 필드 제외 자체 | 소비자 계약이 걸린 **키 집합**만 |
| MyBatis 매핑 자체 | resultType 바인딩이 되는지 | 동적 SQL **분기별 생성 SQL·바인딩 파라미터** |
| Bean Validation 엔진 | `@NotBlank` 가 위반을 잡는지 | 우리가 **선택 필드로 남긴 파라미터**가 검증을 통과하는지(경로 호환) |

> 판별 기준 한 줄: **그 테스트를 깨뜨리려면 우리 코드가 아니라 라이브러리를 고쳐야 하는가?** 그렇다면 TC 대상이 아니다.
