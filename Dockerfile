# -------- Stage 1: Build --------
FROM gradle:8.8-jdk17 AS builder
WORKDIR /app

# Install Node.js 20 (required for Kotlin/JS)
RUN apt-get update && apt-get install -y curl \
    && curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y nodejs \
    && node -v && npm -v

# Copy Gradle wrapper and build files first to leverage Docker cache
COPY gradlew gradlew
COPY gradle gradle
COPY settings.gradle.kts settings.gradle.kts
COPY build.gradle.kts build.gradle.kts

# Make Gradle wrapper executable
RUN chmod +x ./gradlew

# Copy project package.json (inside site folder) and install NPM deps
COPY site/package.json site/package-lock.json ./site/
WORKDIR /app/site
RUN npm ci

# Copy remaining project files
WORKDIR /app
COPY . .

# Build the Kotlin + JS + JVM project
RUN ./gradlew :site:build --no-daemon

# -------- Stage 2: Runtime --------
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copy the JVM JAR from the builder stage
COPY --from=builder /app/site/build/libs/site-1.0-SNAPSHOT.jar app.jar

# Cloud Run expects PORT env
ENV PORT=8080
EXPOSE 8080

CMD ["java", "-jar", "app.jar"]

