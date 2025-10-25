FROM nginx:alpine

# Configure nginx for port 8080
RUN sed -i 's/listen\(.*\)80;/listen 8080;/' /etc/nginx/conf.d/default.conf

# Copy from Kobweb output (the pages directory)
COPY site/.kobweb/site/pages/ /usr/share/nginx/html/

EXPOSE 8080
CMD ["nginx", "-g", "daemon off;"]
