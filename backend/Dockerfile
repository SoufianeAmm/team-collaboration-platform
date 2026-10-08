# --- Build stage ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Cache dependencies separately from source so a code-only change doesn't re-download the world.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# --- Runtime stage ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app
COPY --from=build /build/target/backend-*.jar app.jar
RUN mkdir -p /data/uploads && chown -R app:app /app /data
USER app

ENV DB_PATH=/data/app.db
ENV UPLOAD_DIRECTORY=/data/uploads

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
