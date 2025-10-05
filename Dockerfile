# -------- Stage 1: Builder - Optimized for Caching --------
FROM gradle:8.8-jdk17 AS builder

WORKDIR /app

# 1. Copy only the files needed to download dependencies
COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle ./gradle

# 2. Make the gradlew script executable
RUN chmod +x ./gradlew

# 3. Download dependencies. This layer will be cached as long as your build files don't change.
# Using 'dependencies' is often better than 'build -x test || true' for just fetching dependencies.
RUN ./gradlew dependencies --no-daemon

# 4. Copy the rest of your source code
COPY src ./src

# 5. Now, build the application with the already downloaded dependencies
RUN ./gradlew :site:build --no-daemon

# -------- Stage 2: Runtime - Final Image --------
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Copy the built JAR from the builder stage. Using a wildcard is often more reliable.
COPY --from=builder /app/site/build/libs/*.jar app.jar

# Standard Cloud Run Environment Variables
ENV PORT=8080
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]




