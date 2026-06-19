# syntax=docker/dockerfile:1

### 1) 빌드 스테이지 — 소스에서 bootJar 생성
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY . .
RUN chmod +x gradlew && ./gradlew --no-daemon clean bootJar -x test

### 2) 런타임 스테이지 — JRE만 포함한 경량 이미지
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
# 비루트 사용자로 실행
RUN groupadd -r app && useradd -r -g app app
COPY --from=build /workspace/build/libs/report-front-api-*.jar app.jar
USER app
EXPOSE 8080
# 컨테이너에 할당된 메모리 기준으로 힙을 잡음(Lightsail 소형 번들 대응).
# 필요 시 compose에서 JAVA_OPTS 로 덮어쓸 수 있음.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
