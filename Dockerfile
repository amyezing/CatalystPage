# -------- Stage 1: Builder --------
FROM gradle:8.8-jdk17 AS builder

WORKDIR /app

# Copy Gradle wrapper and build files first for caching
COPY gradlew .
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

# Install Node.js (needed for Kobweb front-end)
RUN apt-get update && \
    apt-get install -y curl gnupg apt-transport-https && \
    curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs

# Download dependencies
RUN chmod +x ./gradlew && ./gradlew dependencies --no-daemon

# Copy full project
COPY . .

# Make gradlew executable (after COPY to avoid permission issues)
RUN chmod +x ./gradlew

# Build the project
RUN ./gradlew :site:build --no-daemon

# -------- Stage 2: Runtime --------
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Copy built JAR from builder
COPY --from=builder /app/site/build/libs/*.jar app.jar

# Cloud Run standard port
ENV PORT=8080
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]

