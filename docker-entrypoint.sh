#!/bin/sh
set -eu

# JWT from Compose secret file (not passed as plain env).
if [ -f /run/secrets/jwt_secret ]; then
  export JWT_SECRET="$(tr -d '\r\n' < /run/secrets/jwt_secret)"
fi

# DB password is set via SPRING_DATASOURCE_PASSWORD in docker-compose.yml (DB_PASSWORD).

exec java -jar /app/app.jar
