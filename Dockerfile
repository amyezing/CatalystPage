# -------- Stage 1: Build --------
FROM gradle:8.8-jdk17 AS builder
WORKDIR /app

# Install Node.js (for JS build) + dos2unix for line endings
RUN apt-get update && apt-get install -y dos2unix curl \
    && curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y nodejs \
    && node -v && npm -v \
    && rm -rf /var/lib/apt/lists/*

# -------------------------------
# STEP 1: Copy only files needed for Gradle dependencies
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle

# Fix line endings and make gradlew executable
RUN dos2unix gradlew && chmod +x gradlew

# Download Gradle dependencies (cached unless build.gradle changes)
RUN ./gradlew --no-daemon build -x test || true

# -------------------------------
# STEP 2: Copy package.json and package-lock.json (Node.js deps)
COPY site/package.json site/package-lock.json ./site/

# Install Node.js dependencies (cached unless package.json changes)
RUN cd site && npm ci

# -------------------------------
# STEP 3: Copy the rest of the project
COPY . .

# Make sure gradlew is still executable
RUN chmod +x gradlew

# Build JVM + JS site
RUN ./gradlew :site:build --no-daemon

# -------- Stage 2: Runtime --------
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copy only the built JAR
COPY --from=builder /app/site/build/libs/com.jar app.jar

# Cloud Run port
ENV PORT=8080
EXPOSE 8080

# Run the app
CMD ["java", "-jar", "app.jar"]



