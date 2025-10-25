FROM gradle:8.8-jdk17 AS builder
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew

# Use Kobweb build command
RUN ./gradlew :site:jsBrowserProductionWebpack

FROM nginx:alpine

# Configure nginx for port 8080 (CRITICAL for Cloud Run)
RUN sed -i 's/listen\(.*\)80;/listen 8080;/' /etc/nginx/conf.d/default.conf

# Copy from Kobweb output - this is where your HTML files with GCS URLs are
COPY --from=builder /app/site/.kobweb/site/pages/ /usr/share/nginx/html/

EXPOSE 8080
CMD ["nginx", "-g", "daemon off;"]