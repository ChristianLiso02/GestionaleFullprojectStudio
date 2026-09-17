#!/bin/sh
set -e

API_URL="${API_BASE_URL:-http://localhost:8080}"

cat > ./static/js/config.js <<EOF
// Generato all'avvio del container da docker-entrypoint.sh a partire da API_BASE_URL.
const API_BASE_URL = "${API_URL}";
EOF

exec java -jar app.jar --spring.web.resources.static-locations=file:./static/,classpath:/static/
