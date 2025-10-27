FROM gradle:8.8-jdk17 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew :site:jsBrowserProductionWebpack :site:jvmJar --no-daemon

# Use nginx base and add JRE
FROM nginx:alpine
RUN apk add --no-cache openjdk17-jre

WORKDIR /app

# Copy backend
COPY --from=builder /app/site/build/libs/catalystpage.com.jar app.jar

# Copy frontend
COPY --from=builder /app/site/.kobweb/site/ /usr/share/nginx/html/

# Configure nginx to proxy API calls to backend
RUN echo 'server {' > /etc/nginx/conf.d/default.conf
RUN echo '    listen 8080;' >> /etc/nginx/conf.d/default.conf
RUN echo '    root /usr/share/nginx/html;' >> /etc/nginx/conf.d/default.conf
RUN echo '    index index.html;' >> /etc/nginx/conf.d/default.conf
RUN echo '    location /api/ {' >> /etc/nginx/conf.d/default.conf
RUN echo '        proxy_pass http://localhost:8081;' >> /etc/nginx/conf.d/default.conf
RUN echo '    }' >> /etc/nginx/conf.d/default.conf
RUN echo '}' >> /etc/nginx/conf.d/default.conf

# Start backend on 8081, nginx on 8080
CMD sh -c "java -jar app.jar -port=8081 & nginx -g 'daemon off;'"