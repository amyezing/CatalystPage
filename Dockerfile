# -------- Stage 1: Builder --------
FROM gradle:8.8-jdk17 AS builder

WORKDIR /app

# Install utilities
RUN apt-get update && apt-get install -y curl dos2unix && rm -rf /var/lib/apt/lists/*

# Copy Gradle wrapper and gradle config
COPY gradlew .
COPY gradle gradle

# Fix line endings and ensure gradlew is executable
RUN dos2unix gradlew && chmod +x gradlew && ls -l gradlew

# Copy only package.json and package-lock.json for Node caching
COPY site/package*.json ./site/

# Install Node.js
RUN curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y nodejs \
    && node -v && npm -v \
    && rm -rf /var/lib/apt/lists/*

# Install Node dependencies
WORKDIR /app/site
RUN npm install
WORKDIR /app

# Copy the rest of the project
COPY . .

# Double-check gradlew permissions again
RUN chmod +x gradlew

# Download Gradle dependencies
RUN ./gradlew --no-daemon build -x test || true

# Build the Kobweb site
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





