# Build stage
FROM gradle:8.5-jdk21 AS builder

WORKDIR /app

# Gradle 캐시 최적화
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon || true

# 소스 코드 복사 및 빌드
COPY src ./src
RUN gradle clean bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# 비root 사용자로 실행
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# 빌드된 JAR 복사
COPY --from=builder /app/build/libs/Board-0.0.1-SNAPSHOT.jar app.jar

# 헬스체크
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 환경변수
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080

EXPOSE 8080

ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", \
    "-jar", \
    "app.jar"]