FROM gradle:8.8-jdk17 AS builder > Dockerfile
WORKDIR /app >> Dockerfile
COPY . . >> Dockerfile
RUN ./gradlew :site:jsBrowserProductionWebpack :site:jvmJar --no-daemon >> Dockerfile
. >> Dockerfile
FROM eclipse-temurin:17-jre-jammy >> Dockerfile
WORKDIR /app >> Dockerfile
. >> Dockerfile
RUN microdnf install nginx >> Dockerfile
. >> Dockerfile
COPY --from=builder /app/site/build/libs/catalystpage.com.jar app.jar >> Dockerfile
COPY --from=builder /app/site/.kobweb/serve/ /usr/share/nginx/html/ >> Dockerfile
. >> Dockerfile
RUN echo 'server { listen 8080; root /usr/share/nginx/html; index index.html; }' > /etc/nginx/nginx.conf >> Dockerfile
. >> Dockerfile
RUN echo '#!/bin/sh' > start.sh >> Dockerfile
RUN echo 'java -jar app.jar &' >> start.sh >> Dockerfile
RUN echo 'nginx -g "daemon off;"' >> start.sh >> Dockerfile
RUN chmod +x start.sh >> Dockerfile
. >> Dockerfile
EXPOSE 8080 >> Dockerfile
CMD ["./start.sh"] >> Dockerfile