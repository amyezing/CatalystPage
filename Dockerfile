# -------- Stage 1: Builder - Optimized for Caching --------
FROM gradle:8.8-jdk17 AS builder

WORKDIR /app

# 1. Copy Gradle wrapper and build files first (for caching)
COPY gradlew .
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

# 2. Install Node.js (needed for Kobweb/JS builds)
RUN apt-get update && \
    apt-get install -y curl gnupg apt-transport-https && \
    curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs && \
    node -v && npm -v

# 3. Download dependencies to cache this layer
RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon

# 4. Copy the full project
COPY . .

# 5. Ensure gradlew is executable (important!)
RUN chmod +x ./gradlew

# 6. Build the site
RUN ./gradlew :site:build --no-daemon

# -------- Stage 2: Runtime - Final Image --------
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/site/build/libs/*.jar app.jar

# Cloud Run standard port
ENV PORT=8080
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]





