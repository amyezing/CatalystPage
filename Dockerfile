# -------- Stage 1: Build --------
FROM gradle:8.8-jdk17 AS builder
WORKDIR /app

# Install Node.js 20
RUN apt-get update && apt-get install -y curl \
    && curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y nodejs \
    && node -v && npm -v

# Copy all project files
COPY . .

# Make Gradle wrapper executable
RUN chmod +x ./gradlew

# Install JS dependencies (if your Kobweb site uses package.json inside site/)
WORKDIR /app/site
RUN npm ci

# Build Kotlin + JS + JVM project
WORKDIR /app
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

