# 트러블슈팅

---

## HikariCP 커넥션 유효성 검증 실패

**증상**
```text
WARN HikariPool-1 - Failed to validate connection ... (This connection has been closed.)
Possibly consider using a shorter maxLifetime value.
```

**원인**

PostgreSQL은 서버 측에서 일정 시간 idle 상태인 커넥션을 끊는다. HikariCP의 기본 `maxLifetime`(30분)이 PostgreSQL의 커넥션 유지 시간보다 길어, 이미 서버에서 끊긴 커넥션을 풀에서 꺼내려다 발생한다.

**해결**

`application.yaml`의 HikariCP 설정을 아래와 같이 조정한다.

```yaml
spring:
  datasource:
    hikari:
      max-lifetime: 600000    # 10분 — PostgreSQL이 끊기 전에 HikariCP가 먼저 폐기
      keepalive-time: 60000   # 1분마다 idle 커넥션에 ping을 보내 살아있는지 확인
```

## 수강신청 마감 스케줄러 동시성 문제 (Race Condition)

**증상 및 원인**
- 정원이 미달된 강의가 스케줄러에 의해 강제 마감(`CLOSED`)되는 현상 발생.
- 스케줄러가 등록 인원을 조회하고 마감 처리하는 사이에 수강 취소가 발생하면, 과거 데이터를 바탕으로 마감해버리는 레이스 컨디션이 발생

**해결**
- 스케줄러 검증 로직에 대상 엔티티 비관적 락(`PESSIMISTIC_WRITE`)을 적용하여 원자성을 보장.
- 루프를 도는 스케줄러 특성상 데드락을 방지하고자, 상태 갱신 로직을 별도 서비스로 분리하고 `@Transactional(propagation = Propagation.REQUIRES_NEW)`를 적용하여 건별 트랜잭션으로 락 점유를 최소화함.

```java
// 대상 전체 조회 후 건별 독립 트랜잭션 실행 (락 누적 방지)
@Scheduled(cron = "0 */5 * * * *")
public void closeFullCourses() {
    for (Course course : courseQueryService.findOpenCoursesWithCapacity()) {
        courseClosingService.closeIfFull(course.getCourseId()); 
    }
}

// 건당 즉시 락을 획득하고 트랜잭션 종료 시(마감 반영 시) 즉시 락 해제
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void closeIfFull(Long courseId) {
    Course lockedCourse = courseQueryService.findByIdForUpdate(courseId); // 비관적 락
    if (enrollmentQueryService.countActive(courseId) >= lockedCourse.getCapacity()) {
        courseCommandService.updateStatus(lockedCourse, CourseStatus.CLOSED);
    }
}
```
