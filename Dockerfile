FROM gradle:8.8-jdk17 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew :site:jsBrowserProductionWebpack :site:jvmJar --no-daemon

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Fixed: Use apt-get for eclipse-temurin (Ubuntu-based)
RUN apt-get update && apt-get install -y nginx && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/site/build/libs/catalystpage.com.jar app.jar
COPY --from=builder /app/site/.kobweb/serve/ /usr/share/nginx/html/

RUN echo 'server { listen 8080; root /usr/share/nginx/html; index index.html; }' > /etc/nginx/nginx.conf

# Direct command - more reliable than shell script
CMD sh -c "java -jar app.jar & nginx -g 'daemon off;'"