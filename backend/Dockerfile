# Stage 1: Build Java application
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

RUN apk add --no-cache gradle

COPY build.gradle settings.gradle gradle.properties ./
COPY src src
COPY build/libs/app.jar* /app/build/libs/

RUN if [ ! -f /app/build/libs/app.jar ]; then gradle bootJar --no-daemon -x test; fi

# Stage 2: Runtime environment
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN apk add --no-cache curl

COPY --from=builder /app/build/libs/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
