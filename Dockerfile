# ---------- 1) Build Stage ----------
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

# Gradle 래퍼와 설정 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew

RUN ./gradlew dependencies --no-daemon

COPY src src

RUN ./gradlew clean bootJar --no-daemon


# ---------- 2) Runtime Stage ----------
FROM eclipse-temurin:25-jre-alpine AS runtime
WORKDIR /app
RUN mkdir -p /app/logs

COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080

# t3.xlarge: 4 vCPU, 16GB RAM
ENV JAVA_OPTS="\
    -Xms4g \
    -Xmx4g \
    -XX:+UseZGC \
    -XX:+ZGenerational \
    -Djdk.virtualThreadScheduler.maxPoolSize=128 \
    -XX:MetaspaceSize=128M \
    -XX:MaxMetaspaceSize=256M \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/app/logs/heapdump.hprof \
    -XX:+ExitOnOutOfMemoryError \
    -Djava.security.egd=file:/dev/./urandom \
    -Xlog:gc*:file=/app/logs/gc.log:time,level,tags:filecount=3,filesize=5M"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
