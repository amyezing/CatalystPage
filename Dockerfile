# -------- Stage 1: Build --------
FROM gradle:8.8-jdk17 AS builder
WORKDIR /app

# Install dos2unix for line ending fixes
RUN apt-get update && apt-get install -y dos2unix

# Copy only Gradle wrapper and build scripts first (for caching)
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle

# Fix line endings and make gradlew executable
RUN dos2unix gradlew && chmod +x gradlew

# Download dependencies (this layer will be cached unless build.gradle changes)
RUN ./gradlew --no-daemon build -x test || true

# Copy the rest of the project
COPY . .

# Build JVM + JS site
RUN ./gradlew :site:build --no-daemon

# -------- Stage 2: Runtime --------
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copy only the JVM JAR built from Stage 1
COPY --from=builder /app/site/build/libs/com.jar app.jar

# Cloud Run port
ENV PORT=8080
EXPOSE 8080

# Run the app
CMD ["java", "-jar", "app.jar"]

