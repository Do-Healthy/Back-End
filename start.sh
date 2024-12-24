#!/bin/bash

# Redis 실행 (백그라운드 모드)
redis-server --daemonize yes

# Java 애플리케이션 실행
java -jar /app/app.jar