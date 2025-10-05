# -------- Stage 1: Builder --------
FROM gradle:8.8-jdk17 AS builder

WORKDIR /app

# Install Node.js and dos2unix for Windows line ending conversion
RUN apt-get update && \
    apt-get install -y curl dos2unix && \
    curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs

# Copy everything
COPY . .

# Fix line endings and permissions for gradlew
RUN dos2unix gradlew && chmod +x gradlew

# Verify it works
RUN ./gradlew --version

# Build the Kobweb project
RUN ./gradlew :site:build --no-daemon

# -------- Stage 2: Runtime --------
FROM openjdk:17-jdk-slim

WORKDIR /app

RUN apt-get update && apt-get install -y ca-certificates && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/site/build/libs/site-all.jar ./site-all.jar

EXPOSE 8080

CMD ["java", "-jar", "site-all.jar"]
