#!/bin/sh
set -eu

# JWT from Compose secret file (not passed as plain env).
if [ -f /run/secrets/jwt_secret ]; then
  export JWT_SECRET="$(tr -d '\r\n' < /run/secrets/jwt_secret)"
fi

if [ -f /run/secrets/db_password ]; then
  export SPRING_DATASOURCE_PASSWORD="$(tr -d '\r\n' < /run/secrets/db_password)"
fi

exec java -jar /app/app.jar
