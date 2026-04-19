# 설계 질문 & 트러블슈팅

설계 과정에서 고민했던 질문과 답변, 그리고 실제 적용 결과를 기록

---

## Q1. API Latency 기준을 어떻게 잡아야 할까?

**질문**

조회 API와 쓰기 API의 허용 응답 시간을 어떻게 나눠야 할까? 단일 기준을 쓰면 조회는 너무 느슨하고 쓰기는 너무 엄격해진다.

**답변**

API 성격에 따라 두 가지로 분류했다.

- **조회 API**: Redis 캐시 히트 시 DB I/O가 없으므로 빠른 응답이 가능하다. 이커머스 현업 기준인 100ms를 참고해 p95 100ms를 목표로 삼았다.
- **쓰기 API**: Redis 분산 락 획득 → DB 트랜잭션 → 응답까지 여러 I/O가 포함되므로 p95 500ms를 목표로 삼았다.

측정은 두 관점에서 한다.

- AOP 로깅: 서버 내부 처리 시간 (500ms 초과 시 WARN 기록)
- k6: 클라이언트 관점 end-to-end 시간. 두 값의 차이가 크면 네트워크·인프라 구간에 병목이 있는 것으로 판단한다.

**실제 적용**

- `ControllerLoggingAspect` — 500ms 초과 요청을 WARN 레벨로 기록
- k6 threshold 설정으로 SLO 기준 초과 시 테스트 실패 처리
- 자세한 지표 정의: [SLI-SLO-SLA.md](SLI-SLO-SLA.md)

---

## Q2. HikariCP 커넥션 풀 크기를 어떻게 결정했나?

**질문**

커넥션 풀 크기를 어떻게 잡아야 할까?

**답변**

HikariCP 공식 문서와 PostgreSQL 권장 공식인 `connections = (core_count * 2) + effective_spindle_count`를 따랐다.

- 배포 대상: t3.xlarge (vCPU 4, SSD 기반 → effective_spindle = 1)
- 계산: `(4 * 2) + 1 = 9` → 반올림하여 **10**
- `minimum-idle = maximum-pool-size = 10`: HikariCP가 권장하는 "fixed pool" 패턴. 유휴 커넥션 반환/재생성 오버헤드를 없애고 풀을 상수 크기로 유지

2000명의 동시 요청은 커넥션 풀 크기와 무관하게 DB 비관락 직렬화 구간에서 병목이 생긴다. 실질적인 해결은 Redis 분산락으로 DB에 진입하는 요청 수를 앞단에서 제한하는 것이다.

**실제 적용**

- `maximum-pool-size: 10`, `minimum-idle: 10` (fixed pool)
- `connection-timeout: 30000ms` — 빠른 실패(fail-fast)가 필요하면 5000ms로 낮추는 것도 고려

---

## Q3. 취소된 수강 신청의 중복 방지를 어떻게 처리할까?

**질문**

`UNIQUE (student_id, class_id)` 단순 제약을 걸면 한 번 취소한 학생은 같은 강의에 재신청할 수 없다. 취소 레코드를 이력으로 보존하면서 재신청은 허용해야 한다.

**답변**

두 가지 방식을 비교했다.

- **(A) 재신청 시 기존 CANCELLED row를 UPDATE**: 이력이 사라져 취소 시점·사유 추적 불가
- **(B) 부분 유니크 인덱스**: 활성 상태(`PENDING`, `CONFIRMED`, `WAITLISTED`)에 한해서만 중복을 금지하고, `CANCELLED` row는 이력으로 누적

PostgreSQL의 partial unique index를 지원하므로 (B)를 선택했다. 이력이 보존되면 "몇 번 취소했는지", "마지막 취소 시점은 언제인지"를 감사·분석 용도로 활용할 수 있고, 과제 B의 정산
계산 시에도 원본 결제/취소 내역이 남아있어야 일관성 있는 집계가 가능하다.

**실제 적용**

```sql
CREATE UNIQUE INDEX UX_ENROLLMENTS_ACTIVE_STUDENT_CLASS
    ON enrollments (student_id, class_id)
    WHERE status IN ('PENDING', 'CONFIRMED', 'WAITLISTED');
```

---
