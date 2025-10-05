# -------- Stage 1: Builder --------
FROM gradle:8.8-jdk17 AS builder

WORKDIR /app

# Copy Gradle wrapper and build files first (for caching)
COPY gradlew .
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

# Fix Windows permission issue
RUN chmod +x ./gradlew

# Install Node.js (needed for Kobweb frontend)
RUN apt-get update && \
    apt-get install -y curl gnupg apt-transport-https && \
    curl -fsSL https://deb.nodesource.com/setup_18.x | bash - && \
    apt-get install -y nodejs && \
    npm install -g kobweb-cli

# Copy the rest of the project
COPY . .

# Build the project (adjust if you use `gradlew kobwebExport`)
RUN ./gradlew kobwebExport --stacktrace

# -------- Stage 2: Runner --------
FROM nginx:stable-alpine

# Copy exported site from builder
COPY --from=builder /app/.kobweb/site /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

