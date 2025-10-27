echo FROM gradle:8.8-jdk17 AS builder > Dockerfile
echo WORKDIR /app >> Dockerfile
echo COPY . . >> Dockerfile
echo RUN ./gradlew :site:jsBrowserProductionWebpack :site:jvmJar --no-daemon >> Dockerfile
echo. >> Dockerfile
echo FROM eclipse-temurin:17-jre-jammy >> Dockerfile
echo WORKDIR /app >> Dockerfile
echo. >> Dockerfile
echo RUN microdnf install nginx >> Dockerfile
echo. >> Dockerfile
echo COPY --from=builder /app/site/build/libs/catalystpage.com.jar app.jar >> Dockerfile
echo COPY --from=builder /app/site/.kobweb/serve/ /usr/share/nginx/html/ >> Dockerfile
echo. >> Dockerfile
echo RUN echo 'server { listen 8080; root /usr/share/nginx/html; index index.html; }' > /etc/nginx/nginx.conf >> Dockerfile
echo. >> Dockerfile
echo RUN echo '#!/bin/sh' > start.sh >> Dockerfile
echo RUN echo 'java -jar app.jar &' >> start.sh >> Dockerfile
echo RUN echo 'nginx -g "daemon off;"' >> start.sh >> Dockerfile
echo RUN chmod +x start.sh >> Dockerfile
echo. >> Dockerfile
echo EXPOSE 8080 >> Dockerfile
echo CMD ["./start.sh"] >> Dockerfile