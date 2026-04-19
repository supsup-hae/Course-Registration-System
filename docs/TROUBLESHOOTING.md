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
