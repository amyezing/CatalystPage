FROM gradle:8.8-jdk17 AS builder
WORKDIR /app
COPY . .

# Build both targets in one command (KMP way)
RUN ./gradlew :site:jsBrowserProductionWebpack :site:jvmJar --no-daemon

# Backend stage
FROM eclipse-temurin:17-jre-jammy as backend
WORKDIR /app
COPY --from=builder /app/site/build/libs/catalystpage.com.jar app.jar
CMD ["java", "-jar", "app.jar"]

# Frontend stage
FROM nginx:alpine as frontend
COPY --from=builder /app/site/.kobweb/site/ /usr/share/nginx/html/
RUN sed -i 's/listen\(.*\)80;/listen 8080;/' /etc/nginx/conf.d/default.conf
CMD ["nginx", "-g", "daemon off;"]