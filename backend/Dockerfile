# Stage 1: Build Java artifact using Gradle wrapper or gradle image
FROM gradle:8.5-jdk21-alpine AS builder
WORKDIR /app
COPY . .
RUN gradle build -x test --no-daemon

# Stage 2: Runtime image using Eclipse Temurin JDK 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache curl
COPY --from=builder /app/build/libs/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
