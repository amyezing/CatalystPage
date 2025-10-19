# -------- Stage 1: Build frontend with Kobweb --------
FROM gradle:8.8-jdk17 AS builder
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew kobwebBuild

FROM nginx:alpine
COPY nginx.conf /etc/nginx/nginx.conf
COPY --from=builder /app/site/.kobweb/site/ /usr/share/nginx/html/
RUN mv /usr/share/nginx/html/pages/* /usr/share/nginx/html/ && rmdir /usr/share/nginx/html/pages
COPY --from=builder /app/site/build/kotlin-webpack/js/productionExecutable/ /usr/share/nginx/html/
COPY --from=builder /app/site/build/processedResources/js/main/public/ /usr/share/nginx/html/
EXPOSE 8080
CMD ["nginx", "-g", "daemon off;"]
EOF