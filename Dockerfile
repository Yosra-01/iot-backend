# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
# Cache dependencies first (improves build time if only code changes)
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Security: Run as a non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

ENV SPRING_PROFILES_ACTIVE=docker
ENV TZ=Africa/Cairo
COPY --from=build /app/target/*.jar app.jar
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh
EXPOSE 8080
ENTRYPOINT ["/docker-entrypoint.sh"]