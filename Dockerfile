# -------- Stage 1: Build --------
FROM gradle:8.8-jdk17 AS builder
WORKDIR /app

# Install Node.js and Yarn
RUN apt-get update && apt-get install -y curl \
    && curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y nodejs \
    && npm install --global yarn \
    && node -v && npm -v && yarn -v

# Copy project files
COPY . .

# Install JS dependencies
WORKDIR /app/site
RUN yarn install --frozen-lockfile

# Build Kotlin project
WORKDIR /app
RUN gradle :site:build --no-daemon

# -------- Stage 2: Runtime --------
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copy the final JAR
COPY --from=builder /app/site/build/libs/com.jar app.jar

# Cloud Run PORT
ENV PORT=8080
EXPOSE 8080

CMD ["java", "-jar", "app.jar"]

