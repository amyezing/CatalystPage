FROM gradle:8.8-jdk17 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew :site:jsBrowserProductionWebpack :site:jvmJar --no-daemon

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN microdnf install nginx

COPY --from=builder /app/site/build/libs/catalystpage.com.jar app.jar
COPY --from=builder /app/site/.kobweb/serve/ /usr/share/nginx/html/

RUN echo 'server { listen 8080; root /usr/share/nginx/html; index index.html; }' > /etc/nginx/nginx.conf

# Better startup script with error handling
RUN echo '#!/bin/sh' > start.sh
RUN echo 'echo "Starting backend..."' >> start.sh
RUN echo 'java -jar app.jar &' >> start.sh
RUN echo 'echo "Backend started with PID: $!"' >> start.sh
RUN echo 'echo "Starting nginx..."' >> start.sh
RUN echo 'nginx -g "daemon off;"' >> start.sh
RUN echo 'echo "Nginx started"' >> start.sh
RUN chmod +x start.sh

EXPOSE 8080
CMD ["./start.sh"]