# 수강 신청 시스템 (Course Registration System)

## 목차

- [프로젝트 개요](#프로젝트-개요)
- [기술 스택](#기술-스택)
- [실행 방법](#실행-방법)
- [API 목록 및 예시](#api-목록-및-예시)
- [데이터 모델 설명](#데이터-모델-설명)
- [요구사항 해석 및 가정](#요구사항-해석-및-가정)
- [프로젝트 구조](#프로젝트-구조)
- [설계 결정과 이유](#설계-결정과-이유)
- [테스트 실행 방법](#테스트-실행-방법)
- [미구현 / 제약사항](#미구현--제약사항)
- [AI 활용 범위](#ai-활용-범위)

---

## 프로젝트 개요

> **과제 A** — 크리에이터(강사)가 강의를 개설하고, 클래스메이트(수강생)가 수강 신청·결제·취소를 수행하는 CRUD + 비즈니스 규칙형 백엔드 서비스
---

## 기술 스택

| 기술 | 버전 | 선택 이유                                                                                                                                                                            |
|------|------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Java | 25 | Kotlin 대비 기존 Java 생태계 호환성이 높고, Virtual Threads 기반 경량 동시성 모델로 OS 스레드를 직접 점유하지 않아 I/O 대기 구간 스레드 고갈 위험을 줄일 수 있다.                                                                    |
| Spring Boot | 4.0.5 | Quarkus·Micronaut 등 경량 프레임워크 대비 생태계 범위와 레퍼런스가 압도적으로 넓다. JPA·Redis·Validation·Security 등 이 프로젝트에 필요한 모든 기능을 생태계 안에서 일관된 방식으로 조합할 수 있고, Java 25 Virtual Threads도 별도 설정 없이 통합 지원한다. |
| Spring Data JPA | (Hibernate) | MyBatis 대비 SQL을 직접 작성하지 않고 객체 중심으로 도메인을 모델링할 수 있다. `@EntityListeners` 기반 Auditing으로 생성일·수정일을 자동 기록하고, `BaseEntity` 상속으로 공통 필드를 한곳에서 관리한다.                                        |
| PostgreSQL | 18.3 | MySQL 대비 MVCC 구현이 정교하고, 행 단위 잠금(`SELECT FOR UPDATE`)과 SKIP LOCKED를 통한 동시성 제어가 가능하다. ACID 트랜잭션을 보장해 정원 초과 방지의 DB 레벨 안전망으로 선택하였다.                                                  |
| Redis | 8.6.2 | Memcached 대비 다양한 자료구조와 TTL 기반 만료 정책을 지원한다. 분산 카운터로 동시 신청 요청을 직렬화해 정원 초과를 방지하고, 강의 상세 조회 결과를 인메모리 캐싱해 반복 DB I/O를 줄인다.                                                             |
| Caffeine | - | Redis 캐시 앞단의 로컬 캐시로, JVM 내 인메모리 캐싱을 통해 Redis 네트워크 왕복 비용 없이 극단적으로 빠른 응답이 필요한 구간에 활용한다.                                                                                            |
| SpringDoc OpenAPI | 3.0.3 | Swagger 애너테이션을 별도로 관리하는 방식과 달리, 코드 변경 시 API 문서가 자동으로 갱신된다. Spring MVC 컨트롤러 구조를 그대로 분석해 문서를 생성하므로 코드와 문서 간 불일치가 발생하지 않는다.                                                         |
| Spring Validation | - | 직접 if 분기로 검증 로직을 작성하는 방식 대비, `@Valid`·`@NotNull`·`@Size` 등 애너테이션 선언만으로 입력 검증을 처리할 수 있다. 검증 실패는 `GlobalExceptionHandler`가 일괄 처리한다.                                                |
| Spring Actuator | - | 별도 APM 없이 `/actuator/health` 등 엔드포인트로 컨테이너 헬스체크 및 기본 운영 지표를 제공한다.                                                                                                                |
| Lombok | - | 반복적인 보일러네이트 코드(getter, constructor, builder 등)를 애너테이션으로 제거해 도메인 로직에 집중할 수 있다.                                                                                                    |
| GitHub Actions | - | Jenkins 대비 별도 서버 없이 GitHub 저장소와 즉시 연동된다. PR 생성 시 빌드·테스트가 자동 실행되어 로컬 환경 차이로 발생하는 문제를 병합 전에 발견할 수 있다.                                                                              |

---

## 실행 방법

### 사전 요구사항

- Docker & Docker Compose

### 1. 환경 변수 설정

프로젝트 루트(`docker-compose.yml`과 같은 위치)에 `.env` 파일을 생성한다.

```
Course-Registration-System/
├── .env                  ← 여기에 생성
├── docker-compose.yml
└── ...
```

```dotenv
POSTGRES_DBNAME=liveklass
POSTGRES_USERNAME=postgres
POSTGRES_PASSWORD=your_password
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password
```

### 2. 전체 실행

```bash
docker-compose up -d
```

DB 초기화 및 앱 기동까지 자동으로 처리된다. 앱 컨테이너는 PostgreSQL/Redis 헬스체크 통과 후 시작된다.

> 테스트 데이터는 [Mockaroo](https://mockaroo.com)로 생성한 `src/main/resources/script/mock-data.sql`이 앱 기동 시 자동으로 적재된다.

### 테스트 계정 (mock-data 기준)

| 역할 | X-User-Id | X-User-Role |
|------|-----------|-------------|
| 크리에이터 | `1` | `CREATOR` |
| 수강생 | `101` | `STUDENT` |

Swagger UI의 **Authorize** 버튼에서 위 값을 입력하면 각 역할로 API를 테스트할 수 있다.

### 3. 접속

| 서비스 | URL |
|--------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |

Swagger에서 API 테스트 시 우측 상단 **Authorize** 버튼에서 `X-User-Id`, `X-User-Role` 헤더를 설정한다.

---

## API 목록 및 예시

### 강의

---

#### 강의 등록

- **POST** `/api/v1/courses` | 권한: `CREATOR`

**Request**
```bash
curl -X 'POST' \
  'http://localhost:8080/api/v1/courses' \
  -H 'accept: application/json' \
  -H 'X-User-Id: 1' \
  -H 'X-User-Role: CREATOR' \
  -H 'Content-Type: application/json' \
  -d '{
  "title": "크리투스 전문코치 1:1 코칭 프로그램",
  "description": "크리에이터 성장 올인원 패키지 수강생을 위한 1:1 코칭입니다.",
  "price": 50000,
  "capacity": 10,
  "startDate": "2026-05-01T10:00:00",
  "endDate": "2026-05-01T12:00:00"
}'
```

**Response** `201`
```json
{
  "success": true,
  "data": {
    "courseId": 17,
    "status": "DRAFT"
  },
  "error": null,
  "timestamp": "2026-04-21 21:43:25"
}
```

---

#### 강의 상태 변경

- **PATCH** `/api/v1/courses/{courseId}/status` | 권한: `CREATOR`

| 구분 | 파라미터 | 타입 | 예시 | 설명 |
|------|----------|------|------|------|
| Path | `courseId` | Long | `1` | 강의 ID |

**Request**
```bash
curl -X 'PATCH' \
  'http://localhost:8080/api/v1/courses/17/status' \
  -H 'accept: application/json' \
  -H 'X-User-Id: 1' \
  -H 'X-User-Role: CREATOR' \
  -H 'Content-Type: application/json' \
  -d '{
  "status": "OPEN",
  "startDate": "2026-05-01T00:00:00",
  "endDate": "2026-06-01T00:00:00"
}'
```

**Response** `200`
```json
{
  "success": true,
  "data": {
    "courseId": 17,
    "status": "OPEN",
    "startDate": "2026-05-01T00:00:00",
    "endDate": "2026-06-01T00:00:00"
  },
  "error": null,
  "timestamp": "2026-04-21 21:34:57"
}
```

---

#### 강의 상세 조회

- **GET** `/api/v1/courses/{courseId}` | 권한: 전체

| 구분 | 파라미터 | 타입 | 예시 | 설명 |
|------|----------|------|------|------|
| Path | `courseId` | Long | `1` | 강의 ID |

**Request**
```bash
curl -X 'GET' \
  'http://localhost:8080/api/v1/courses/17' \
  -H 'accept: application/json'
```

**Response** `200`
```json
{
  "success": true,
  "data": {
    "courseId": 17,
    "creator": {
      "userId": 1,
      "name": "Verile Malthus",
      "email": "vmalthus0@goodreads.com",
      "role": "CREATOR"
    },
    "title": "크리투스 전문코치 1:1 코칭 프로그램",
    "description": "크리에이터 성장 올인원 패키지 수강생을 위한 1:1 코칭입니다.",
    "price": 50000,
    "capacity": 10,
    "status": "OPEN",
    "startDate": "2026-05-01T00:00:00",
    "endDate": "2026-06-01T00:00:00",
    "createdAt": "2026-04-21T12:43:25.76052",
    "currentEnrollmentCount": 0
  },
  "error": null,
  "timestamp": "2026-04-21 21:52:07"
}
```

---

#### 강의 목록 조회

- **GET** `/api/v1/courses` | 권한: 전체

| 구분 | 파라미터 | 타입 | 기본값 | 예시 | 설명 |
|------|----------|------|--------|------|------|
| Query | `page` | int | `0` | `0` | 페이지 번호 |
| Query | `size` | int | `10` | `10` | 페이지 크기 (1~100) |
| Query | `status` | String | - | `OPEN` | `DRAFT` / `OPEN` / `CLOSED` |
| Query | `minPrice` | BigDecimal | - | `10000` | 최소 수강료 |
| Query | `maxPrice` | BigDecimal | - | `50000` | 최대 수강료 |
| Query | `hasCapacity` | Boolean | - | `true` | 정원 제한 여부 |

**Request**
```bash
curl -X 'GET' \
  'http://localhost:8080/api/v1/courses?page=0&size=10&status=OPEN&minPrice=10000&maxPrice=50000&hasCapacity=true' \
  -H 'accept: application/json'
```

**Response** `200`
```json
{
  "content": [
    {
      "courseId": 13,
      "creator": {
        "userId": 1,
        "name": "Verile Malthus"
      },
      "title": "초보자를 위한 SQL 핵심 가이드 백서",
      "price": 25000,
      "status": "OPEN"
    },
    {
      "courseId": 10,
      "creator": {
        "userId": 1,
        "name": "Verile Malthus"
      },
      "title": "파이썬 첫걸음과 데이터 분석",
      "price": 40000,
      "status": "OPEN"
    },
    {
      "courseId": 6,
      "creator": {
        "userId": 1,
        "name": "Verile Malthus"
      },
      "title": "자바 기초부터 실무까지 완벽 가이드",
      "price": 45000,
      "status": "OPEN"
    },
    {
      "courseId": 1,
      "creator": {
        "userId": 1,
        "name": "Verile Malthus"
      },
      "title": "Spring Boot Mastery",
      "price": 50000,
      "status": "OPEN"
    },
    {
      "courseId": 7,
      "creator": {
        "userId": 1,
        "name": "Verile Malthus"
      },
      "title": "리액트(React)로 만드는 모던 웹 프론트엔드",
      "price": 50000,
      "status": "OPEN"
    }
  ],
  "hasNext": false,
  "totalPages": 1,
  "totalElements": 5,
  "page": 0,
  "size": 10,
  "isFirst": true,
  "isLast": true
}
```

---

#### 강의별 수강생 목록 조회

- **GET** `/api/v1/courses/{courseId}/enrollments` | 권한: `CREATOR`

| 구분 | 파라미터 | 타입 | 기본값 | 예시 | 설명 |
|------|----------|------|--------|------|------|
| Path | `courseId` | Long | - | `1` | 강의 ID |
| Query | `page` | int | `0` | `0` | 페이지 번호 |
| Query | `size` | int | `10` | `10` | 페이지 크기 (1~100) |
| Query | `status` | String | - | `CONFIRMED` | `PENDING` / `CONFIRMED` / `CANCELLED` |
| Query | `sortOrder` | String | `DESC` | `DESC` | 정렬 순서 (`ASC` / `DESC`) |

**Request**
```bash
curl -X 'GET' \
  'http://localhost:8080/api/v1/courses/1/enrollments?page=0&size=10&sortOrder=DESC' \
  -H 'accept: application/json' \
  -H 'X-User-Id: 1' \
  -H 'X-User-Role: CREATOR'
```


**Response** `200`
```json
{
  "content": [
    {
      "enrollmentId": 1,
      "student": {
        "userId": 101,
        "name": "Roxanna Franchi"
      },
      "status": "CONFIRMED",
      "enrolledAt": "2026-04-21T12:46:25.917808"
    }
  ],
  "hasNext": false,
  "totalPages": 1,
  "totalElements": 1,
  "page": 0,
  "size": 10,
  "isFirst": true,
  "isLast": true
}
```

---

### 수강신청

---

#### 수강신청 생성

- **POST** `/api/v1/enrollments` | 권한: `STUDENT`

**Request**
```bash
curl -X 'POST' \
  'http://localhost:8080/api/v1/enrollments' \
  -H 'accept: application/json' \
  -H 'X-User-Id: 101' \
  -H 'X-User-Role: STUDENT' \
  -H 'Content-Type: application/json' \
  -d '{
  "courseId": 17
}'
```

**Response** `201`
```json
{
  "success": true,
  "data": {
    "enrollmentId": 2,
    "courseId": 17,
    "studentId": 101,
    "status": "PENDING",
    "expiresAt": "2026-04-21T13:09:47.081049976"
  },
  "error": null,
  "timestamp": "2026-04-21 21:54:47"
}
```

---

#### 수강신청 확정

- **PATCH** `/api/v1/enrollments/{id}/confirm` | 권한: `STUDENT`

| 구분 | 파라미터 | 타입 | 예시 | 설명 |
|------|----------|------|------|------|
| Path | `id` | Long | `100` | 수강신청 ID |

**Request**
```bash
curl -X 'PATCH' \
  'http://localhost:8080/api/v1/enrollments/2/confirm' \
  -H 'accept: application/json' \
  -H 'X-User-Id: 101' \
  -H 'X-User-Role: STUDENT'
```

**Response** `200`
```json
{
  "success": true,
  "data": {
    "enrollmentId": 2,
    "courseId": 17,
    "studentId": 101,
    "status": "CONFIRMED",
    "expiresAt": "2026-04-21T13:09:47.08105"
  },
  "error": null,
  "timestamp": "2026-04-21 21:55:18"
}
```

---

#### 수강신청 취소

- **PATCH** `/api/v1/enrollments/{id}/cancel` | 권한: `STUDENT`

| 구분 | 파라미터 | 타입 | 예시 | 설명 |
|------|----------|------|------|------|
| Path | `id` | Long | `100` | 수강신청 ID |

**Request**
```bash
curl -X 'PATCH' \
  'http://localhost:8080/api/v1/enrollments/2/cancel' \
  -H 'accept: application/json' \
  -H 'X-User-Id: 101' \
  -H 'X-User-Role: STUDENT'
```


**Response** `200`
```json
{
  "success": true,
  "data": {
    "enrollmentId": 2,
    "courseId": 17,
    "studentId": 101,
    "status": "CANCELLED",
    "expiresAt": "2026-04-21T13:09:47.08105"
  },
  "error": null,
  "timestamp": "2026-04-21 21:56:58"
}
```

---

### 사용자

---

#### 내 수강신청 목록 조회

- **GET** `/api/v1/users/me/enrollments` | 권한: `STUDENT`

| 구분 | 파라미터 | 타입 | 기본값 | 예시 | 설명 |
|------|----------|------|--------|------|------|
| Query | `page` | int | `0` | `0` | 페이지 번호 |
| Query | `size` | int | `10` | `10` | 페이지 크기 (1~100) |
| Query | `status` | String | - | `CONFIRMED` | `PENDING` / `CONFIRMED` / `CANCELLED` |
| Query | `sortOrder` | String | `DESC` | `DESC` | 정렬 순서 (`ASC` / `DESC`) |


**Request**
```bash
curl -X 'GET' \
  'http://localhost:8080/api/v1/users/me/enrollments?page=0&size=10&sortOrder=ASC' \
  -H 'accept: application/json' \
  -H 'X-User-Id: 101' \
  -H 'X-User-Role: STUDENT'
```

**Response** `200`
```json
{
  "content": [
    {
      "enrollmentId": 1,
      "course": {
        "courseId": 1,
        "creator": {
          "userId": 1,
          "name": "Verile Malthus"
        },
        "title": "Spring Boot Mastery",
        "price": 50000,
        "status": "OPEN"
      },
      "status": "CONFIRMED"
    },
    {
      "enrollmentId": 2,
      "course": {
        "courseId": 17,
        "creator": {
          "userId": 1,
          "name": "Verile Malthus"
        },
        "title": "크리투스 전문코치 1:1 코칭 프로그램",
        "price": 50000,
        "status": "OPEN"
      },
      "status": "CANCELLED"
    },
    {
      "enrollmentId": 3,
      "course": {
        "courseId": 17,
        "creator": {
          "userId": 1,
          "name": "Verile Malthus"
        },
        "title": "크리투스 전문코치 1:1 코칭 프로그램",
        "price": 50000,
        "status": "OPEN"
      },
      "status": "CONFIRMED"
    },
    {
      "enrollmentId": 4,
      "course": {
        "courseId": 2,
        "creator": {
          "userId": 1,
          "name": "Verile Malthus"
        },
        "title": "JPA Advanced Topics",
        "price": 60000,
        "status": "DRAFT"
      },
      "status": "CONFIRMED"
    },
    {
      "enrollmentId": 5,
      "course": {
        "courseId": 3,
        "creator": {
          "userId": 1,
          "name": "Verile Malthus"
        },
        "title": "Redis Caching Strategies",
        "price": 40000,
        "status": "DRAFT"
      },
      "status": "CONFIRMED"
    },
    {
      "enrollmentId": 6,
      "course": {
        "courseId": 4,
        "creator": {
          "userId": 1,
          "name": "Verile Malthus"
        },
        "title": "MSA Architecture with Spring Cloud",
        "price": 80000,
        "status": "CLOSED"
      },
      "status": "CONFIRMED"
    },
    {
      "enrollmentId": 8,
      "course": {
        "courseId": 6,
        "creator": {
          "userId": 1,
          "name": "Verile Malthus"
        },
        "title": "자바 기초부터 실무까지 완벽 가이드",
        "price": 45000,
        "status": "OPEN"
      },
      "status": "PENDING"
    },
    {
      "enrollmentId": 7,
      "course": {
        "courseId": 5,
        "creator": {
          "userId": 1,
          "name": "Verile Malthus"
        },
        "title": "Java 25 Best Practices",
        "price": 0,
        "status": "OPEN"
      },
      "status": "PENDING"
    },
    {
      "enrollmentId": 9,
      "course": {
        "courseId": 7,
        "creator": {
          "userId": 1,
          "name": "Verile Malthus"
        },
        "title": "리액트(React)로 만드는 모던 웹 프론트엔드",
        "price": 50000,
        "status": "OPEN"
      },
      "status": "PENDING"
    }
  ],
  "hasNext": false,
  "totalPages": 1,
  "totalElements": 9,
  "page": 0,
  "size": 10,
  "isFirst": true,
  "isLast": true
}
```

## 데이터 모델 설명

### ERD

<!-- ERD 캡처 이미지 -->

<img src="src/main/resources/image/img.png" alt="ERD" width="50%">

### 테이블 설명

#### `users`

| 컬럼                          | 타입           | 설명                    |
|-----------------------------|--------------|-----------------------|
| `user_id`                   | BIGINT (PK)  | 자동 증가 식별자             |
| `name`                      | VARCHAR(100) | 사용자 이름                |
| `email`                     | VARCHAR(255) | 로그인 이메일 (유니크)         |
| `password`                  | VARCHAR(255) | 비밀번호                  |
| `role`                      | VARCHAR(20)  | `STUDENT` / `CREATOR` |
| `created_at` / `updated_at` | TIMESTAMP    | JPA Auditing 자동 기록    |

#### `courses`

| 컬럼                          | 타입                  | 설명                          |
|-----------------------------|---------------------|-----------------------------|
| `course_id`                 | BIGINT (PK)         | 자동 증가 식별자                   |
| `creator_id`                | BIGINT (FK → users) | 강의를 개설한 크리에이터               |
| `title`                     | VARCHAR(255)        | 강의 제목                       |
| `description`               | TEXT                | 강의 설명                       |
| `price`                     | NUMERIC(12, 0)      | 수강료 (원 단위)                  |
| `capacity`                  | INT (nullable)      | 최대 수강 정원 (`null` = 무제한)     |
| `status`                    | VARCHAR(20)         | `DRAFT` / `OPEN` / `CLOSED` |
| `start_date` / `end_date`   | TIMESTAMP           | 수강 기간                       |
| `created_at` / `updated_at` | TIMESTAMP           | JPA Auditing 자동 기록          |

#### `enrollments`

| 컬럼                          | 타입                    | 설명                                                   |
|-----------------------------|-----------------------|------------------------------------------------------|
| `enrollment_id`             | BIGINT (PK)           | 자동 증가 식별자                                            |
| `student_id`                | BIGINT (FK → users)   | 수강 신청한 학생                                            |
| `course_id`                 | BIGINT (FK → courses) | 신청 대상 강의                                             |
| `status`                    | VARCHAR(20)           | `PENDING` / `CONFIRMED` / `CANCELLED` / `WAITLISTED` |
| `waitlist_order`            | INT (nullable)        | 대기열 순서 (WAITLISTED 상태일 때만 사용)                        |
| `expires_at`                | TIMESTAMP (nullable)  | PENDING 만료 일시 (기본 15분)                               |
| `confirmed_at`              | TIMESTAMP (nullable)  | 결제 확정 시각                                             |
| `cancelled_at`              | TIMESTAMP (nullable)  | 취소 처리 시각                                             |
| `cancelled_reason`          | VARCHAR(100) (nullable) | 취소 사유                                               |
| `created_at` / `updated_at` | TIMESTAMP             | JPA Auditing 자동 기록                                   |

### 주요 제약 조건

- `courses.capacity IS NULL OR capacity > 0` — CHECK 제약 (`null` = 무제한 정원)
- `courses.price >= 0` — CHECK 제약
- `enrollments (student_id, course_id) WHERE status IN ('PENDING', 'CONFIRMED', 'WAITLISTED')` — 부분 유니크 인덱스로 활성 상태 중복 신청
  방지, CANCELLED 이력은 보존 ([Q3](docs/QUESTIONS.md))

---

## 요구사항 해석 및 가정

> [!NOTE]
> 명세에 명시되지 않았거나 해석이 필요한 부분을 아래와 같이 결정하였다.

| 항목 | 해석 및 결정 |
|------|-------------|
| **강의 등록** | - `CREATOR` 역할만 등록 가능<br>- 정원(`capacity`) `null` = 무제한<br>- 수강 기간 `null` = 무기한 |
| **강의 상태 전환** | - 허용: `DRAFT → OPEN`, `OPEN → CLOSED`, `CLOSED → OPEN` (재오픈)<br>- 금지: 모든 상태에서 `DRAFT` 복귀, `DRAFT → CLOSED` 직행 |
| **강의 날짜** | - 등록(`DRAFT`) 시 미리 설정 가능<br>- `OPEN` 전환 시 날짜 지정하면 덮어쓰고, 생략하면 기존 값 유지<br>- `startDate`/`endDate` 모두 지정 시 `endDate > startDate` 검증 |
| **강의 상세 조회** | - 현재 수강 인원(`currentEnrollmentCount`) 포함<br>- Redis 캐싱으로 반복 DB I/O 최소화, `GzipRedisSerializer`로 캐시 메모리 최적화 |
| **강의 목록 조회** | - 상태·가격 범위·빈자리 여부(`hasCapacity`) 필터 및 페이징 지원 |
| **결제 확정** | - 외부 PG 연동 없이 `PATCH /enrollments/{id}/confirm` 호출로 `CONFIRMED` 상태 전환으로 대체 |
| **수강신청 만료** | - `PENDING` 상태에서 15분 내 미확정 시 스케줄러가 `CANCELLED`로 전환<br>- 선점된 Redis 슬롯 롤백 처리 |
| **동시성 정원 관리** | - Redis Counter로 1차 슬롯 선점<br>- DB 저장 시 Pessimistic Lock으로 2차 검증하는 2중 구조로 구현 |
| **취소 기간 제한** | - 명세의 "결제 후 7일 이내 취소 가능" 요건 구현 완료 |
| **사용자 인증** | - JWT·OAuth 없이 `X-User-Id` / `X-User-Role` 헤더를 파싱해 `SecurityContext`에 주입 (명세 허용 방식) |

---

## 프로젝트 구조

```
src/main/java/com/liveklass/
├── common/                   # 도메인 횡단 공통 레이어
│   ├── aop/                  # 실행 시간 로깅
│   ├── cache/                # 캐시
│   ├── config/               # JPA / Redis / Security / Swagger 설정
│   ├── entity/               # BaseEntity
│   ├── error/                # 공통 예외 처리기
│   ├── response/             # 공통 응답
│   ├── security/             # Spring Security
│   └── util/
└── domain/
    ├── course/               # 강의
    ├── enrollment/           # 수강신청
    └── user/                 # 유저

# 각 도메인 공통 구조
{domain}/
├── controller/               # command / query 분리
├── service/                  # facade / command / query 분리 (CQRS)
├── entity/
├── repository/
├── dto/                      # request / response / common
├── converter/
├── enums/
└── exception/
```

---

## 설계 결정과 이유

### CQRS 서비스 레이어 분리

| 레이어 | 역할 | 원칙 |
|--------|------|------|
| `CommandService` | 단일 엔티티 저장·수정·삭제 | 쓰기 전용, 조회 금지 |
| `QueryService` | 단일 엔티티 조회 | 읽기 전용(`readOnly = true`) |
| `FacadeService` | Command / Query 조합 및 비즈니스 흐름 오케스트레이션 | Controller가 유일하게 의존하는 진입점 |

읽기와 쓰기 책임을 명확히 분리해 각 서비스의 역할 범위를 최소화하고, 트랜잭션 최적화(`readOnly`) 및 테스트 격리를 용이하게 한다.

### Facade 패턴 도입

CQRS로 서비스를 분리하면 여러 Command / Query를 조합하는 비즈니스 흐름을 처리할 주체가 필요하다. 이를 Controller에서 직접 처리하면 Controller가 내부 서비스 구조를 알게 되어 책임이 분산된다.

| 구분 | 역할 |
|------|------|
| `Controller` | 요청/응답 변환만 담당. `FacadeService`만 호출 |
| `FacadeService` | 내부 Command / Query 서비스를 오케스트레이션 |
| `Command / QueryService` | 단일 책임 수행. Facade 내부 구조는 Controller에 노출되지 않음 |

### 캐시 2계층 구조 (Local + Redis)

단일 Redis 캐시만 사용하면 모든 조회 요청이 네트워크를 경유한다. 이를 줄이기 위해 JVM 내 Caffeine(로컬)과 Redis(글로벌)를 계층화했다.

| 계층 | 구현체 | 특징 |
|------|--------|------|
| 1계층 (로컬) | Caffeine | JVM 내 인메모리, 네트워크 비용 없음. 인스턴스 내 일관성만 보장 |
| 2계층 (글로벌) | Redis | 인스턴스 간 공유. 로컬 미스 시 조회 후 로컬 캐시 갱신 |

조회 순서: `Local Hit → 즉시 반환` / `Local Miss → Redis 조회 → Local 갱신 → 반환`

> 트레이드오프: 멀티 인스턴스 환경에서 로컬 캐시 간 일시적 불일치가 발생할 수 있다. 단기 정합성보다 조회 성능이 우선인 데이터에 한해 허용 가능한 트레이드오프로 판단했다.

### 동시성 제어 — Redis INCR + Lua Script

정원 초과 방지를 위해 DB 레벨 비관적 락만 사용하면 모든 요청이 DB 트랜잭션을 경유해 커넥션 풀 고갈 위험이 있다. 이를 줄이기 위해 Redis를 1차 선점 게이트로 정의.

| 단계 | 방식 | 역할 |
|------|------|------|
| 1차 선점 | Redis `INCR` + Lua Script | 원자적으로 카운터 증가 후 정원 초과 여부 즉시 판단. 초과 시 DB 진입 차단 |
| 2차 검증 | DB Pessimistic Lock | Redis 통과 요청만 DB 트랜잭션 진입, 최종 정합성 보장 |

Lua Script를 사용한 이유: `INCR` 후 정원 비교를 별도 명령으로 처리하면 두 명령 사이에 다른 요청이 끼어드는 Race Condition이 발생한다. Lua Script는 Redis 서버에서 원자적으로 실행되므로 이 구간을 제거할 수 있다.

---

## 테스트 실행 방법

### 단위 / 통합 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 단일 클래스 실행
./gradlew test --tests "com.liveklass.domain.course.service.CourseFacadeServiceTest"
```

> 인프라(PostgreSQL · Redis) 없이 실행 가능하다. 테스트 환경은 `@DataJpaTest` / `@WebMvcTest` 슬라이스 및 `EmbeddedRedis`를 사용한다.

### 테스트 구성

| 레이어 | 주요 테스트 클래스 |
|--------|-----------------|
| Controller | `CourseCommandControllerTest`, `CourseQueryControllerTest`, `EnrollmentCommandControllerTest`, `UserQueryControllerTest` |
| Facade | `CourseFacadeServiceTest`, `EnrollmentFacadeServiceTest`, `UserFacadeServiceTest` |
| Service | `CourseCommandServiceImplTest`, `EnrollmentCommandServiceImplTest`, `EnrollmentQueryServiceImplTest` |
| Entity | `CourseTest`, `EnrollmentTest` |
| DTO | `RegisterCourseReqDtoTest` |

### 부하 테스트 (k6)

사전 조건: 앱 실행 중(`docker-compose up -d`), k6 설치 필요

```bash
k6 run src/test/k6/script/scenario1.js
k6 run src/test/k6/script/scenario2.js
k6 run src/test/k6/script/scenario3.js
```

| 시나리오 | 스크립트 | 결과 | 목표 | p95 | Error Rate | 처리량 | SLO |
|----------|----------|------|------|-----|------------|--------|-----|
| 1 — 강의 상세 조회 | [`scenario1.js`](src/test/k6/script/scenario1.js) | [`결과`](src/test/k6/results/시나리오1.md) | 조회 p95 < 100ms, > 500 req/s | **2.01ms** | **0%** | **631 req/s** | ✅ |
| 2 — 동시 수강신청 | [`scenario2.js`](src/test/k6/script/scenario2.js) | [`결과`](src/test/k6/results/시나리오2(현재%20신청%20인원%20조회%20O).md) | 정원 초과 거부 정확도 100% | **39ms** | 11%* | - | ✅ |
| 3 — 수강신청 인원 조회 | [`scenario3.js`](src/test/k6/script/scenario3.js) | [`결과`](src/test/k6/results/시나리오3.md) | 조회 p95 < 100ms, > 500 req/s | **4.03ms** | **0%** | **630 req/s** | ✅ |

> \* 시나리오 2 에러율은 정원 초과로 의도적으로 거부된 409 응답이 포함된 수치로, 실제 5xx 오류가 아닌 정상 동작이다.

---

## 미구현 / 제약사항

### 필수 구현 완료 항목

| 항목 | API |
|------|-----|
| 강의 등록 | `POST /api/v1/courses` |
| 강의 상태 관리 (`DRAFT` / `OPEN` / `CLOSED`) | `PATCH /api/v1/courses/{courseId}/status` |
| 강의 목록 조회 (상태 필터) | `GET /api/v1/courses` |
| 강의 상세 조회 (현재 신청 인원 포함) | `GET /api/v1/courses/{courseId}` |
| 수강 신청 | `POST /api/v1/enrollments` |
| 결제 확정 처리 | `PATCH /api/v1/enrollments/{id}/confirm` |
| 수강 취소 | `PATCH /api/v1/enrollments/{id}/cancel` |
| 내 수강 신청 목록 조회 | `GET /api/v1/users/me/enrollments` |
| 정원 초과 신청 거부 (동시성 포함) | Redis Counter + Pessimistic Lock |

### 선택 구현 (추가 점수) 현황

| 항목 | 구현 완료 여부 | 비고 |
|------|--------------|------|
| 수강 취소 가능 기간 제한 (결제 후 7일 이내) | ✅ | |
| 대기열(waitlist) 기능 | ❌ | 아래 설계 참고 |
| 강의별 수강생 목록 조회 (크리에이터 전용) | ✅ | `GET /api/v1/courses/{courseId}/enrollments` |
| 신청 내역 페이지네이션 | ✅ | `GET /api/v1/users/me/enrollments` |

#### 대기열(Waitlist) 미구현 설계 방향

**1. 선착순 순서 보장**
- 정원이 찬 시점부터 신청자를 대기열에 등록하되, 신청 시각을 기준으로 순서를 보장한다.
- Redis Sorted Set은 Score 기반 자동 정렬을 제공하므로, 별도 순번 관리 없이 선착순 구조를 유지할 수 있다.

**2. 슬롯 반환 시 자동 승격**
- 취소 또는 결제 미확정 만료로 빈 자리가 생기면, 대기열에서 가장 앞선 순번의 신청자를 자동으로 승격한다.
- 승격된 신청자는 일반 신청과 동일한 결제 대기 흐름(PENDING → CONFIRMED)으로 진입한다.

**3. 기존 만료 처리 시스템과 연계**
- 이미 구현된 PENDING 만료 스케줄러가 슬롯 반환 이벤트를 트리거하는 구조로 확장할 수 있다.
- 별도의 대기열 전용 스케줄러 없이 기존 흐름 안에서 대기열 소비를 처리할 수 있어 구조가 단순하게 유지된다.

### 제약사항

| 항목 | 내용 |
|------|------|
| 이메일/알림 | 신청·확정·취소 이벤트에 대한 알림 미구현 |
| 강의 삭제 | 과제 명세 미포함 항목으로 미구현 |
| 인증 구조 | `X-User-Id` / `X-User-Role` 헤더 직접 신뢰 방식. 실제 서비스라면 JWT 기반 검증 필요 |
| 트러블슈팅 | 운영 중 발생한 이슈 및 해결 내역: [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md) |

#### SLO / SLA

> 전체 목표 및 측정 기준은 [docs/SLI-SLO-SLA.md](docs/SLI-SLO-SLA.md) 참고

| 항목 | 목표 |
|------|------|
| 조회 API p95 | 100ms 이하 (Redis 캐시 히트 전제) |
| 쓰기 API p95 | 500ms 이하 (Redis 분산 락 + DB 트랜잭션 포함) |
| 정원 오신청 | 0건 (동시 신청 시 초과 허용 없음) |
| Error Rate (5xx) | 1% 미만 |

#### Checkstyle — 네이버 코딩 컨벤션 기반 직접 정의

별도로 합의된 팀 컨벤션이 없는 상황에서, 국내 Java 생태계에서 가장 널리 참조되는 [네이버 코딩 컨벤션 v1.2](https://naver.github.io/hackday-conventions-java/)를 기반으로 프로젝트 전용 규칙(`config/checkstyle/liveklass-checkstyle-rules.xml`)을 직접 정의하였다.

코드 리뷰 시 스타일 지적 비용을 없애고, 규칙 위반 시 **빌드 자체를 실패**시켜 컨벤션 준수를 자동으로 강제한다.

| 규칙 | 내용 |
|------|------|
| 네이밍 | 패키지 소문자, 클래스 UpperCamelCase, 변수·메서드 lowerCamelCase |
| 임포트 | `*` import 금지, 그룹별 정렬 강제 |
| 중괄호 | K&R 스타일, 단문 블록에도 `{}` 필수 |
| 들여쓰기 | 탭(4칸) 사용, 스페이스 혼용 금지 |
| 줄 길이 | 최대 120자 |
| 선언 | 한 줄에 하나의 구문·변수 선언만 허용 |


---

## AI 활용 범위

| 분류            | 도구                                        | 활용 내용                                                                                                                   |
|---------------|-------------------------------------------|-------------------------------------------------------------------------------------------------------------------------|
| DTO · Swagger | Github Copilot (Gemini 3.1 Pro)           | 요청/응답 DTO 필드 초안 생성, `@Operation` · `@Schema` 어노테이션 작성 보조                                                                |
| 문서 작성         | Claude Sonnet 4.6                         | README 초안 및 구조 설계, Git 커밋 컨벤션 문서 작성                                                                                     |
| PR 자동화        | Claude Code 커스텀 스킬                        | 브랜치 커밋·변경 파일 분석 후 PR 드래프트 자동 생성 ([`skills/create-pr-draft-without-jira`](skills/create-pr-draft-without-jira/skill.md)) |
| 동시성 구조 검토     | Claude Code                               | Redis Counter + Pessimistic Lock 2단계 구조 직접 설계 후, Race Condition 발생 구간 및 트랜잭션 경계 검증 보조                                   |
| 보안 검토         | Claude Code (`superpowers:code-reviewer`) | 헤더 인증 우회 가능성, Race Condition, 입력값 검증 누락 점검                                                                              |
| 코드 리뷰         | CodeRabbit + Claude Code                  | PR 단위 자동 리뷰 (버그·성능·보안), SOLID·DRY 기준 개선 제안, 논리적 버그 발견 및 수정                                                              |

> [!NOTE]
> AI가 생성한 코드는 직접 검토·수정 후 반영하였습니다.
