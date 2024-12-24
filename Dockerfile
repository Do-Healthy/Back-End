# Java를 기본으로 시작
FROM openjdk:17-jdk-slim

# Redis 설치
RUN apt-get update && apt-get install -y redis-server

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 빌드 결과물 복사 (애플리케이션 JAR 파일)
COPY build/libs/SearchSpace-0.0.1-SNAPSHOT.jar app.jar

# 포트 열기 (애플리케이션과 Redis)
EXPOSE 8080
EXPOSE 6379

# Redis와 Java 애플리케이션 실행 스크립트 복사
COPY start.sh /app/start.sh
RUN chmod +x /app/start.sh

# 스크립트 실행
CMD ["/app/start.sh"]