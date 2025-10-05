# -------- Stage 1: Build --------
FROM gradle:8.8-jdk17 AS builder
WORKDIR /app

# Install Node.js for Kotlin/JS
RUN apt-get update && apt-get install -y curl \
    && curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y nodejs \
    && node -v && npm -v

# Copy all project files
COPY . .

# Make Gradle wrapper executable
RUN chmod +x ./gradlew

# Install NPM dependencies inside the site folder
WORKDIR /app/site
RUN npm install

# Build the entire project (JS + JVM)
WORKDIR /app
RUN ./gradlew :site:build --no-daemon

# -------- Stage 2: Runtime --------
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copy the JVM JAR (wildcard ensures it works regardless of version)
COPY --from=builder /app/site/build/libs/*.jar app.jar

# Cloud Run will inject PORT
ENV PORT=8080
EXPOSE 8080

# Start the application
CMD ["java", "-jar", "app.jar"]

