# -------- Stage 1: Build --------
FROM gradle:8.8-jdk17 AS builder
WORKDIR /app

# ------------------------------
# Install Node.js (for Kotlin/JS)
# ------------------------------
RUN apt-get update && apt-get install -y curl \
    && curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y nodejs \
    && node -v && npm -v

# ------------------------------
# Copy Gradle wrapper and config first (for caching)
# ------------------------------
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x ./gradlew

# ------------------------------
# Copy JS dependencies first (for caching npm install)
# ------------------------------
WORKDIR /app/site
COPY site/package*.json ./
RUN npm install

# ------------------------------
# Copy the rest of the site JS code
# ------------------------------
COPY site/ ./

# ------------------------------
# Copy remaining project files and build JVM + JS
# ------------------------------
WORKDIR /app
COPY . .
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

