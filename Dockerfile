FROM gradle:8.8-jdk17 AS builder
WORKDIR /app
COPY . .

# Build the fat JAR
RUN ./gradlew :site:fatJar --no-daemon

FROM nginx:alpine
RUN apk add --no-cache openjdk17-jre

WORKDIR /app

# Copy the fat JAR (correct name with version)
COPY --from=builder /app/site/build/libs/catalyst-backend-1.0-SNAPSHOT-all.jar app.jar

# Copy frontend
COPY --from=builder /app/site/.kobweb/site/ /usr/share/nginx/html/

# Simple nginx config
RUN echo 'server { listen 8080; root /usr/share/nginx/html; index index.html; }' > /etc/nginx/conf.d/default.conf

# Start backend
CMD sh -c "java -jar app.jar & nginx -g 'daemon off;'"