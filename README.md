# Task Manager

Backend application for team task management.

## Requirements
- Java 17
- Maven 3.9.9
- Docker
- PostgreSQL

## Run
1. docker compose up -d
2. mvn clean verify
3. run "TaskManagerApplication"

### PostgreSQL:
localhost:5432

### Application:
localhost:8080

### Health:
localhost:8080/actuator/health