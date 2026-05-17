# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=docker
ENV TZ=Africa/Cairo

COPY --from=build /app/target/*.jar app.jar
COPY docker-entrypoint.sh /docker-entrypoint.sh

RUN mkdir -p /var/lib/iotmonitor/profile-pictures \
  && sed -i 's/\r$//' /docker-entrypoint.sh \
  && chmod 755 /docker-entrypoint.sh \
  && addgroup -S spring && adduser -S spring -G spring \
  && chown -R spring:spring /app /docker-entrypoint.sh /var/lib/iotmonitor

USER spring:spring

EXPOSE 8080
ENTRYPOINT ["/docker-entrypoint.sh"]
