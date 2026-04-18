# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Environment

- **OS**: Windows. `coderabbit` 관련 스킬(`coderabbit:code-reviewer`, `coderabbit:autofix`) 호출 시에는 반드시 **WSL Ubuntu** 기반으로
  실행한다.
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
./gradlew test --tests "com.liveklass.FullyQualifiedTestClass"

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

패키지 구조: `com.liveklass.<domain>.{controller,service,repository,domain}`

### Common Layer (`com.liveklass.common`)

모든 도메인이 의존하는 공통 인프라:

- **`response/BaseResponse<T>`** — 모든 API 응답 래퍼. `ok()`, `created()`, `noContent()`, `fail()` 팩토리 메서드 사용.
  `ResponseUtils`로 `ResponseEntity` 변환.
- **`error/ErrorCode`** — `C-001~C-009` 공통 에러 코드 enum. 도메인별 에러는 별도 enum으로 추가 후 `GlobalExceptionHandler`에 등록.
- **`error/exception/BusinessException`** — 비즈니스 로직 예외의 기반 클래스. `ErrorCode`를 받아 throw.
- **`entity/BaseEntity`** — JPA Auditing 기반 `createdAt` / `updatedAt` 자동 관리. 모든 엔티티가 상속.
- **`aop/`** — Controller(500ms 초과 시 WARN), Service 실행 시간 자동 로깅. 별도 설정 불필요.

### API Response 패턴

```java
// Controller 반환 예시
return ResponseUtils.toResponseEntity(BaseResponse.ok(data));
	return ResponseUtils.

toResponseEntity(BaseResponse.created(data));
```

### Swagger

`http://localhost:8080/swagger-ui.html` — `SwaggerConfig`에서 설정.

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

## Checkstyle

`config/checkstyle/liveklass-checkstyle-rules.xml` 적용 (main 소스만). 위반 시 빌드 실패.
