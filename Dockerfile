# Stage 1: Frontend build
FROM gradle:8.8-jdk17 AS frontend-builder
WORKDIR /app
COPY . .
RUN ./gradlew :site:jsBrowserProductionWebpack --no-daemon

# Stage 2: Backend build
FROM gradle:8.8-jdk17 AS backend-builder
WORKDIR /app
COPY . .
RUN ./gradlew :site:fatJar --no-daemon

# Stage 3: Runtime
FROM nginx:alpine
RUN apk add --no-cache openjdk17-jre

WORKDIR /app

# Copy backend
COPY --from=backend-builder /app/site/build/libs/catalyst-backend-1.0-SNAPSHOT-all.jar app.jar

# Copy frontend
COPY --from=frontend-builder /app/site/.kobweb/site/pages/ /usr/share/nginx/html/

# Configure nginx
RUN cat > /etc/nginx/conf.d/default.conf << 'EOF'
server {
    listen 8080;
    location / {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
    location /health {
        proxy_pass http://localhost:8080;
    }
}
EOF

CMD sh -c "java -jar app.jar & nginx -g 'daemon off;'"