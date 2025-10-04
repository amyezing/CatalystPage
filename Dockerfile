# -------- Stage 1: Build --------
FROM gradle:8.8-jdk17 AS builder
WORKDIR /app

# Install Node.js (for Kotlin/JS)
RUN apt-get update && apt-get install -y curl \
    && curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y nodejs \
    && node -v && npm -v

# Copy only Gradle build files first (cache dependencies)
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY site/build.gradle.kts site/package.json site/yarn.lock ./site/

# Pre-install NPM dependencies in site module (caching)
WORKDIR /app/site
RUN gradle :kotlinNpmInstall --no-daemon

# Copy the rest of the project
WORKDIR /app
COPY . .

# Build the project
RUN gradle :site:build --no-daemon

# -------- Stage 2: Runtime --------
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copy the final JAR
COPY --from=builder /app/site/build/libs/com.jar app.jar

# Cloud Run will inject PORT
ENV PORT=8080
EXPOSE 8080

# Run the app
CMD ["java", "-jar", "app.jar"]
