FROM gradle:8.8-jdk17 AS builder
WORKDIR /app
COPY . .

# Build the fat JAR
RUN ./gradlew :site:fatJar --no-daemon

FROM nginx:alpine
RUN apk add --no-cache openjdk17-jre

WORKDIR /app

# Copy the fat JAR
COPY --from=builder /app/site/build/libs/catalyst-backend-1.0-SNAPSHOT-all.jar app.jar

# Copy frontend
COPY --from=builder /app/site/.kobweb/site/ /usr/share/nginx/html/

# Configure nginx to proxy API calls to backend
RUN cat > /etc/nginx/conf.d/default.conf << 'EOF'
server {
    listen 8080;

    # Serve frontend files
    location / {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # Proxy API calls to backend
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # Proxy health check to backend
    location /health {
        proxy_pass http://localhost:8080;
    }
}
EOF

# Start backend and nginx
CMD sh -c "java -jar app.jar & nginx -g 'daemon off;'"