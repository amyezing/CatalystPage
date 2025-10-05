# -------- Stage 1: Build --------
FROM gradle:8.8-jdk17 AS builder

WORKDIR /app

# Install utilities
RUN apt-get update && apt-get install -y curl dos2unix && rm -rf /var/lib/apt/lists/*

# Copy only gradlew and gradle wrapper first, fix permissions
COPY gradlew .
COPY gradle gradle
RUN dos2unix gradlew && chmod +x gradlew

# Copy the rest of the project
COPY . .

# Install Node.js
RUN curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y nodejs \
    && node -v && npm -v \
    && rm -rf /var/lib/apt/lists/*

# Download Gradle dependencies (cache)
RUN ./gradlew --no-daemon build -x test || true

# Build the site
RUN ./gradlew :site:build --no-daemon

# -------- Stage 2: Runtime --------
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Install bash
RUN apt-get update && apt-get install -y bash && rm -rf /var/lib/apt/lists/*

# Copy the built JAR
COPY --from=builder /app/site/build/libs/$(ls /app/site/build/libs | grep -v 'metadata\|klib' | head -n1) app.jar

# Cloud Run port
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]








