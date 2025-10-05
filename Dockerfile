# -------- Stage 1: Build --------
FROM gradle:8.8-jdk17 AS builder
WORKDIR /app

# Ensure Unix line endings support
RUN apt-get update && apt-get install -y curl dos2unix \
    && rm -rf /var/lib/apt/lists/*

# Copy Gradle wrapper + config
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle

# Fix gradlew line endings + make executable
RUN dos2unix gradlew && chmod +x gradlew

# Download Gradle dependencies (cache)
RUN ./gradlew --no-daemon build -x test || true

# Copy Node.js dependency files
COPY site/package.json site/package-lock.json ./site/

# Install Node.js
RUN curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y nodejs \
    && node -v && npm -v \
    && rm -rf /var/lib/apt/lists/*

# Copy the rest of the project
COPY . .

# Ensure gradlew still executable
RUN dos2unix gradlew && chmod +x gradlew

# Build JVM + JS site
RUN ./gradlew :site:build --no-daemon

# -------- Stage 2: Runtime --------
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Dynamically pick main JAR (exclude metadata/klib)
RUN apt-get update && apt-get install -y bash
COPY --from=builder /app/site/build/libs/$(ls /app/site/build/libs | grep -v 'metadata\|klib' | head -n1) app.jar

# Cloud Run port
ENV PORT=8080
EXPOSE 8080

# Run the app
CMD ["java", "-jar", "app.jar"]






