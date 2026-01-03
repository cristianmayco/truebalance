# Multi-stage build
FROM gradle:8.11.1-jdk21-alpine AS builder

WORKDIR /app

# Copy gradle files
COPY build.gradle settings.gradle ./
COPY gradle gradle
COPY gradlew gradlew.bat ./

# Copy source code
COPY src src

# Build the application
RUN ./gradlew clean build -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the jar from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]