# -------- Stage 1: Builder --------
FROM gradle:8.8-jdk17 AS builder

WORKDIR /app

# Copy the whole project first
COPY . .

# Ensure gradlew is executable
RUN chmod +x gradlew && ls -l gradlew

# Download Gradle dependencies (optional cache step)
RUN ./gradlew --no-daemon build -x test || true

# Build the Kobweb site
RUN ./gradlew :site:build --no-daemon

# -------- Stage 2: Runtime --------
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/site/build/libs/$(ls /app/site/build/libs | grep -v 'metadata\|klib' | head -n1) app.jar

# Cloud Run port
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]






