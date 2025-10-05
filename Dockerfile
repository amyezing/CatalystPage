# -------- Stage 1: Builder --------
FROM gradle:8.8-jdk17 AS builder

WORKDIR /app

# Install utilities
RUN apt-get update && apt-get install -y dos2unix && rm -rf /var/lib/apt/lists/*

# Copy Gradle wrapper and gradle configuration
COPY gradlew .
COPY gradle gradle

# Fix line endings and ensure gradlew is executable
RUN dos2unix gradlew && chmod +x gradlew && ls -l gradlew

# Copy the whole project
COPY . .

# Download Gradle dependencies (cached)
RUN ./gradlew --no-daemon build -x test || true

# Build the Kobweb site
RUN ./gradlew :site:build --no-daemon

# -------- Stage 2: Runtime --------
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Install bash
RUN apt-get update && apt-get install -y bash && rm -rf /var/lib/apt/lists/*

# Copy the built JAR from builder stage
COPY --from=builder /app/site/build/libs/$(ls /app/site/build/libs | grep -v 'metadata\|klib' | head -n1) app.jar

# Cloud Run port
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]





