# Git Commit Convention

## Required: Use Unicode Gitmoji

**IMPORTANT**: Always use Unicode emoji characters (✨, ♻️, 🐛, etc.), NOT text codes (`:sparkles:`, `:recycle:`,
`:bug:`).

### Gitmoji Reference Table

| **Emoji** | **Type** | **When to Use**                                 |
|-----------|----------|-------------------------------------------------|
| 🎉        | init     | Begin a project (initial commit)                |
| ✨         | feat     | Introduce new features                          |
| ♻️        | refactor | Refactor code                                   |
| 🔥        | remove   | Remove code or files                            |
| 🐛        | fix      | Fix a bug                                       |
| ✅         | test     | Add or update tests                             |
| 📝        | docs     | Add or update documentation                     |
| ➕         | deps     | Add a dependency                                |
| ➖         | deps     | Remove a dependency                             |
| 🔧        | config   | Add or update configuration files               |
| ⏪         | revert   | Revert changes                                  |
| 🚚        | rename   | Move or rename resources (files, paths, routes) |
| 💡        | comment  | Add or update comments in source code           |

## Branch Naming Convention

```
main
develop
feature/<issue-number>
fix/<issue-number>
refactor/<issue-number>
```

### Examples

```
feature/1
feature/42
fix/13
refactor/7
```

## Commit Message Format

```
<gitmoji> <type>(#<issue-number>): <subject>

<body>
```

### Structure Requirements

- **Line 1**: `<gitmoji> <type>(#<issue-number>): <subject>` (max 50 characters)
- **Line 2**: Empty line
- **Line 3+**: Body (bullet points with `-`)

## Examples

### Initial Commit Example

```
🎉 init(#1): Spring Boot 애플리케이션 초기 설정

- Spring Boot 3.x 프로젝트 생성
- 기본 의존성 및 디렉토리 구조 설정
- application.yml 기본 설정 추가
```

### Backend Example

```
♻️ refactor(#32): Spring AI 호환성 업데이트

- Spring AI 1.0.0 GA 버전 대응 의존성 업데이트
- 불필요한 로그 제거 및 메시지 포맷 개선
```

### Config Example

```
🔧 config(#5): docker-compose 설정 수정

- PostgreSQL 18+ 볼륨 경로 변경
- Redis requirepass 환경변수 적용
```

## Subject Line Rules

### ✅ CORRECT Examples

```
🎉 init(#1): Spring Boot 애플리케이션 초기 설정
✨ feat(#12): 사용자 인증 추가
♻️ refactor(#32): Spring AI 호환성 업데이트
🐛 fix(#8): 로그인 리다이렉트 오류 해결
📝 docs(#3): API 문서 업데이트
```

### ❌ INCORRECT Examples

```
✨ feat(#12): 사용자 인증 추가.             // ❌ No period at end
✨ Feat(#12): 사용자 인증 추가              // ❌ Type must be lowercase
✨ feat(#12): 사용자 인증을 추가한다         // ❌ Use imperative mood (명령형)
✨ feat(#12): 사용자 인증을 추가했습니다      // ❌ Use imperative mood, not past tense
✨ feat: 사용자 인증 추가                   // ❌ Missing issue number
```

### Subject Line Requirements

1. **Max 50 characters** (한글 기준 약 25자 이내)
2. **Start with gitmoji** (Unicode character, not text)
3. **Type in lowercase** (feat, fix, refactor, etc.)
4. **Include issue number** in format `(#<number>)`
5. **Use imperative mood** (명령형): "추가" not "추가한다" or "추가했다"
6. **No period** at the end (마침표 금지)
7. **Write in Korean** (subject and body)

## Body Rules

### Format

- Use `-` (dash) to list changes
- Each bullet point on a new line
- Focus on **WHY** and **WHAT** changed
- Be specific and concise

### ✅ CORRECT Example

```
- Spring AI 1.0.0 GA 버전 대응 의존성 업데이트
- 불필요한 디버그 로그 제거
- 에러 메시지 포맷 개선
```

### ❌ INCORRECT Example

```
뭔가 업데이트하고 고침                   // ❌ Too vague
- 의존성 업데이트.                      // ❌ No period at end of bullets
- 로그를 제거했습니다                    // ❌ Use imperative mood, not past tense
```

## Type Selection: Logical Judgment

커밋 타입은 **변경의 표면(파일 종류)이 아닌, 변경의 본질(목적과 성격)**으로 판단한다.

### 판단 기준

| 상황                              | 타입         |
|---------------------------------|------------|
| 사용자가 직접 사용하는 새 기능               | `feat`     |
| 애플리케이션이 동작하기 위한 뼈대/인프라 구성       | `config`   |
| 기능은 그대로, 구조만 개선                 | `refactor` |
| 문서·명세서만 변경                      | `docs`     |
| 여러 성격이 섞인 경우 → **지배적인 목적**으로 결정 | 다수결        |

### 혼합 변경 시 지배적 목적 판단 방법

1. 파일들을 성격별로 묶는다 (CI/인프라/공통레이어/문서 등)
2. 각 묶음이 "왜 존재하는가"를 한 문장으로 정의한다
3. 모든 묶음이 하나의 상위 목적으로 귀결되면 → 그 목적의 타입 사용
4. 귀결되지 않으면 → 커밋을 논리적 단위로 분리한다

### 예시: 공통 인프라 구성 커밋

다음 파일들이 한꺼번에 변경된 경우:

- `.github/workflows/`, `.coderabbit.yaml`, `PULL_REQUEST_TEMPLATE.md` → CI/협업 설정
- `docker-compose.yml`, `config/redis.conf` → 개발 환경 인프라
- `aop/`, `error/`, `response/`, `util/` → Spring 공통 레이어
- `docs/*.pdf` → 명세 문서

→ 모두 "애플리케이션이 동작하고 개발될 수 있는 뼈대를 세운다"는 하나의 목적  
→ `feat`(새 기능)이 아닌 `config`(환경·인프라 구성)로 판단

```
🔧 config(#1): 공통 인프라 및 개발 환경 설정

- AOP 기반 Controller/Service 로깅 구성
- 공통 응답 형식(BaseResponse, ErrorResponse, PageResponse) 추가
- 전역 예외 처리기 및 ErrorCode 정의
- BaseEntity, ResponseUtils, LoggingUtils 유틸리티 추가
- Swagger(SpringDoc) 설정 추가
- Docker Compose 및 Redis 개발 환경 구성
- GitHub Actions Gradle 빌드 워크플로우 추가
- PR 템플릿 및 커밋 컨벤션 문서 추가
- CodeRabbit 코드리뷰 자동화 설정
```

## Quick Checklist

Before committing, verify:

- [ ] Unicode gitmoji used (not text code)
- [ ] Type is lowercase
- [ ] Issue number included: `(#<number>)`
- [ ] Subject is under 50 characters
- [ ] Subject uses imperative mood
- [ ] No period at end of subject
- [ ] Empty line after subject
- [ ] Body uses `-` for bullet points
- [ ] Type reflects the **purpose**, not the file type
- [ ] Mixed changes are grouped by dominant intent (or split into separate commits)
