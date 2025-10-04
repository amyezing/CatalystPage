# -------- Stage 1: Build --------
FROM gradle:8.8-jdk17 AS builder
WORKDIR /app

# Copy all files
COPY . .

# Install npm dependencies for the frontend
WORKDIR /app/site
RUN npm install

# Back to project root and build the Kotlin site
WORKDIR /app
RUN gradle :site:build --no-daemon

# -------- Stage 2: Runtime --------
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copy only the final JAR from builder
COPY --from=builder /app/site/build/libs/com.jar app.jar

# Cloud Run will inject PORT, default 8080 if not provided
ENV PORT=8080
EXPOSE 8080

# Run the app
CMD ["java", "-jar", "app.jar"]