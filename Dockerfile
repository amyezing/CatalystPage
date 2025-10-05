# -------- Stage 1: Builder --------
FROM gradle:8.8-jdk17 AS builder

WORKDIR /app

# Install Node.js first (needed for Kobweb)
RUN apt-get update && \
    apt-get install -y curl gnupg apt-transport-https && \
    curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs && \
    node -v && npm -v

# Copy everything
COPY . .

# Force make gradlew executable and verify
RUN chmod +x ./gradlew && \
    ls -la ./gradlew && \
    echo "Checking if gradlew is executable..." && \
    ./gradlew --version || echo "Gradlew is not executable"

# Build the Kobweb project (produces fat JAR)
RUN ./gradlew :site:build --no-daemon

# -------- Stage 2: Runtime --------
FROM openjdk:17-jdk-slim

WORKDIR /app

# Install certificates for HTTPS support
RUN apt-get update && apt-get install -y ca-certificates && rm -rf /var/lib/apt/lists/*

# Copy the built fat JAR from the builder stage
COPY --from=builder /app/site/build/libs/site-all.jar ./site-all.jar

# Expose the default Cloud Run port
EXPOSE 8080

# Run the app
CMD ["java", "-jar", "site-all.jar"]



