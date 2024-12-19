# 빌드 스테이지
FROM gradle:7.6-jdk11 AS build
WORKDIR /app

# 소스 코드 복사 및 패키지 빌드
COPY . .
RUN mvn package

# 실행 스테이지
FROM openjdk:11-jdk-slim
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=build /app/target/Diet-0.0.1-SNAPSHOT.jar app.jar

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]