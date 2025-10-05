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

# Change to site directory (where JS code lives)
WORKDIR /app/site

# Install NPM dependencies
RUN npm install

# Build the entire site (JS + JVM)
WORKDIR /app
RUN ./gradlew :site:build --no-daemon

# -------- Stage 2: Runtime --------
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copy only the JVM JAR
COPY --from=builder /app/site/build/libs/com.jar app.jar

# Cloud Run will inject PORT
ENV PORT=8080
EXPOSE 8080

CMD ["java", "-jar", "app.jar"]


