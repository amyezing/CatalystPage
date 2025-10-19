FROM gradle:8.8-jdk17 AS builder
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew

# Build production bundle without starting server
RUN ./gradlew jsBrowserProductionWebpack

FROM nginx:alpine
COPY nginx.conf /etc/nginx/nginx.conf

# Copy from different build output locations
COPY --from=builder /app/site/build/processedResources/js/main/public/ /usr/share/nginx/html/
COPY --from=builder /app/site/build/distributions/ /usr/share/nginx/html/
COPY --from=builder /app/site/build/kotlin-webpack/js/productionExecutable/ /usr/share/nginx/html/

EXPOSE 8080
CMD ["nginx", "-g", "daemon off;"]