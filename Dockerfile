# -------- Stage 1: Builder --------
FROM gradle:8.8-jdk17 AS builder

WORKDIR /app

# Copy Gradle wrapper & build files first for caching
COPY gradlew .
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

# Install Node.js (needed for Kobweb)
RUN apt-get update && \
    apt-get install -y curl gnupg apt-transport-https && \
    curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs && \
    node -v && npm -v

# Make gradlew executable and download dependencies
RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon

# Copy full project
COPY . .

# Ensure gradlew is executable (again, just in case)
RUN chmod +x ./gradlew

# Build the site
RUN ./gradlew :site:build --no-daemon

# -------- Stage 2: Runtime --------
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Copy built JAR from builder stage
COPY --from=builder /app/site/build/libs/*.jar app.jar

# Cloud Run standard port
ENV PORT=8080
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
