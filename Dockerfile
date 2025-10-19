FROM gradle:8.8-jdk17 AS builder
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew

# Use the correct Kobweb export task
RUN ./gradlew kobwebExport

FROM nginx:alpine
COPY nginx.conf /etc/nginx/nginx.conf

# Copy from Kobweb export output
COPY --from=builder /app/site/.kobweb/site/ /usr/share/nginx/html/

# Move HTML files from pages/ to root
RUN mv /usr/share/nginx/html/pages/* /usr/share/nginx/html/ && rmdir /usr/share/nginx/html/pages

# Copy compiled Kotlin/JS application
COPY --from=builder /app/site/build/kotlin-webpack/js/productionExecutable/ /usr/share/nginx/html/

# Copy static resources
COPY --from=builder /app/site/build/processedResources/js/main/public/ /usr/share/nginx/html/

EXPOSE 8080
CMD ["nginx", "-g", "daemon off;"]