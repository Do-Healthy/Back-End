# Build Stage
FROM gradle:7.6-jdk17 AS build
WORKDIR /app

# Copy YML and Source Code
COPY Back-End-do-healthy/api-module/src/main/resources/application-test.yml /app/src/main/resources/
COPY . .

RUN gradle build --no-daemon -x test

# Execution Stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]