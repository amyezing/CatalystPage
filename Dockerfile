# -------- Stage 1: Build --------
FROM gradle:8.8-jdk17 AS builder
WORKDIR /app

# Install Node.js for Kotlin/JS
RUN apt-get update && apt-get install -y curl \
    && curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y nodejs \
    && node -v && npm -v

# Copy project files
COPY . .

# Make Gradle wrapper executable
RUN chmod +x ./gradlew

# Build the entire site (JS + JVM)
RUN ./gradlew :site:build --no-daemon

# -------- Stage 2: Runtime --------
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copy only the JVM JAR produced by Gradle
COPY --from=builder /app/site/build/libs/*.jar app.jar

# Cloud Run will inject PORT
ENV PORT=8080
EXPOSE 8080

# Run the app
CMD ["java", "-jar", "app.jar"]
