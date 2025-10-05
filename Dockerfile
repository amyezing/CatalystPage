# -------- Stage 1: Builder --------
FROM gradle:8.8-jdk17 AS builder

WORKDIR /app

# Install Node.js first (needed for Kobweb)
RUN apt-get update && \
    apt-get install -y curl && \
    curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs

# Copy everything
COPY . .

# Force make gradlew executable and test it
RUN chmod +x ./gradlew && ls -la ./gradlew

# Build the Kobweb project
RUN ./gradlew :site:build --no-daemon

# -------- Stage 2: Runtime --------
FROM openjdk:17-jdk-slim

WORKDIR /app

RUN apt-get update && apt-get install -y ca-certificates && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/site/build/libs/site-all.jar ./site-all.jar

EXPOSE 8080

CMD ["java", "-jar", "site-all.jar"]


