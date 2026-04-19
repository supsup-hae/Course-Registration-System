# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Environment

- **OS**: Windows. `coderabbit` 관련 스킬(`coderabbit:code-reviewer`, `coderabbit:autofix`) 호출 시에는 반드시 **WSL Ubuntu** 기반으로 실행한다.
- **Java**: 25 (Temurin)
- **Spring Boot**: 4.0.5
- **DB**: PostgreSQL 18.3 (Docker), Redis 8.6.2 (Docker)

## Commands

```bash
# 인프라 실행 (PostgreSQL + Redis)
docker-compose up -d

# 빌드
./gradlew clean build

# 테스트 전체 실행
./gradlew test

# 단일 테스트 실행
./gradlew test --tests "com.liveklass.domain.course.service.CourseFacadeServiceTest"

# Checkstyle 검사
./gradlew checkstyleMain

# 빌드 (테스트 제외)
./gradlew build -x test
```

## Environment Variables

`.env` 파일이 프로젝트 루트에 필요하다. Spring Boot가 자동으로 로드하지 않으므로, IDE Run Configuration 또는 `EnvFile` 플러그인으로 주입해야 한다.

```
POSTGRES_DBNAME=liveklass
POSTGRES_USERNAME=postgres
POSTGRES_PASSWORD=...
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=...
```

## Architecture

패키지 구조: `com.liveklass.<domain>.{controller,service/{command,query,facade},repository,entity,dto,converter,exception}`

### Common Layer (`com.liveklass.common`)

- **`response/BaseResponse<T>`** — 모든 API 응답 래퍼. `ok()`, `created()`, `noContent()`, `fail()` 팩토리 메서드 사용. `ResponseUtils`로 `ResponseEntity` 변환.
- **`error/ErrorCode`** — 공통(`C-xxx`), 사용자(`U-xxx`), 강의(`CO-xxx`) 에러코드 enum. 도메인별 에러는 별도 enum으로 추가 후 `GlobalExceptionHandler`에 등록.
- **`error/exception/BusinessException`** — 비즈니스 예외 기반 클래스. 도메인별 예외는 이를 상속한다 (예: `UserException`, `CourseException`).
- **`entity/BaseEntity`** — JPA Auditing 기반 `createdAt` / `updatedAt` 자동 관리. 모든 엔티티가 상속.
- **`config/JpaAuditingConfig`** — `@EnableJpaAuditing`을 `LiveklassApplication`에서 분리한 전용 설정 클래스. `@WebMvcTest` 환경에서 JPA metamodel 오류를 방지하기 위함.
- **`aop/`** — Controller(500ms 초과 시 WARN), Service 실행 시간 자동 로깅. `BusinessException`은 최초 발생 지점에서만 WARN 1회 로깅 (`markLogged()` 패턴).
- **`security/`** — `HeaderAuthenticationFilter`가 `X-User-Id` / `X-User-Role` 헤더를 파싱해 `SecurityContext`에 `UserPrincipal`을 주입. Controller에서 `@AuthenticationPrincipal UserPrincipal`로 꺼낸다.

### Service 계층 패턴 (CQRS)

도메인 서비스는 세 레이어로 분리한다:

| 레이어 | 역할 |
|--------|------|
| `CommandService` / `CommandServiceImpl` | 단일 엔티티 저장·수정·삭제 |
| `QueryService` / `QueryServiceImpl` | 단일 엔티티 조회 |
| `FacadeService` | 여러 Command/Query Service를 조합해 비즈니스 흐름 처리 |

Controller는 반드시 `FacadeService`만 직접 호출한다.

### API Response 패턴

```java
return ResponseUtils.created(data);   // 201
return ResponseUtils.ok(data);        // 200
return ResponseUtils.noContent();     // 204
```

### 인증/인가

- `SecurityConfig`에서 `HeaderAuthenticationFilter`를 `UsernamePasswordAuthenticationFilter` 앞에 등록.
- Controller 메서드에 `@PreAuthorize("hasRole('CREATOR')")` 등으로 역할 제한.
- Swagger에서 테스트 시 `X-User-Id`, `X-User-Role` 헤더를 전역으로 입력할 수 있다 (`http://localhost:8080/swagger-ui.html`).

### 엔티티 연관관계 규칙

- 연관관계는 필요할 때만 추가한다 (YAGNI). 현재 `Course → User`, `Enrollment → User`, `Enrollment → Course` 단방향 `@ManyToOne(fetch = FetchType.LAZY)`만 존재.
- 양방향(`@OneToMany`)은 실제로 컬렉션 탐색이 필요한 시점에 추가한다.

### DB 스키마 규칙

- enum 컬럼은 PostgreSQL enum 타입이 아닌 `VARCHAR(20) + CHECK` 제약으로 정의한다 (`init.sql` 참고).
- `courses.capacity NULL` = 무제한 정원. `capacity > 0` CHECK는 `capacity IS NULL OR capacity > 0`으로 정의.

## Git Convention

`.github/git-commit-instructions.md` 참고. 핵심 규칙:

- **형식**: `<gitmoji> <type>(#<이슈번호>): <제목>` (최대 50자)
- **Gitmoji**: 텍스트 코드(`:sparkles:`) 금지, Unicode 문자(`✨`) 사용
- **제목**: 명령형 한국어, 마침표 금지
- **브랜치**: `feature/<번호>`, `fix/<번호>`, `refactor/<번호>`

| Emoji | Type     |
|-------|----------|
| ✨     | feat     |
| 🐛    | fix      |
| ♻️    | refactor |
| 🔧    | config   |
| ✅     | test     |
| 📝    | docs     |

## PR Convention

`.github/PULL_REQUEST_TEMPLATE.md` 양식 사용. Review comment prefix: `P1`(필수 반영), `P2`(적극 고려), `P3`(의견 제안).

## 코드 품질 관점

코드를 작성하거나 리뷰할 때 아래 기준을 기본으로 적용한다.

**SOLID**
- 단일 책임: Controller는 요청/응답 변환만, FacadeService는 흐름 조합만, CommandService는 저장·수정만 담당한다.
- 개방/폐쇄: 새 도메인 예외는 `BusinessException`을 상속해 추가하고, `GlobalExceptionHandler`에 핸들러를 등록한다. 기존 핸들러를 수정하지 않는다.
- 의존성 역전: Service 레이어는 구현체가 아닌 인터페이스(`CourseCommandService`, `UserQueryService`)에 의존한다.

**DRY**
- 공통 응답 포맷은 `BaseResponse` / `ResponseUtils`로 일원화한다. Controller에서 직접 `ResponseEntity`를 생성하지 않는다.
- 테스트 픽스처가 중복되면 `private` 헬퍼 메서드(Object Mother 패턴)로 추출한다.
- 에러 메시지는 `ErrorCode` enum에만 정의한다.

**Clean Code**
- 주석은 WHY가 비자명할 때만 작성한다. WHAT을 설명하는 주석은 금지.
- 메서드는 한 가지 일만 한다. 검증 로직은 `validate*()` 메서드로 분리한다 (`CourseFacadeService.validateDateRange()` 참고).
- 매직 넘버·매직 문자열은 상수로 추출한다.

## Checkstyle

`config/checkstyle/liveklass-checkstyle-rules.xml` 적용 (main 소스만). 위반 시 빌드 실패. 주요 규칙:

- 메서드명·변수명 lowerCamelCase, 언더스코어 금지
- 줄 길이 최대 120자
- `*` import 금지
