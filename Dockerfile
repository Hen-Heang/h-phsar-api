# --- Build stage ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependencies separately from source so a source-only change doesn't
# re-download the whole repository on every build.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# --- Runtime stage ---
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN addgroup --system app && adduser --system --ingroup app app
COPY --from=build /app/target/h-phsar-0.0.1-SNAPSHOT.jar app.jar
USER app

# Render injects PORT at runtime; server.port in application.yaml defaults to
# 8080 but reads ${PORT:8080}, so this stays correct either way.
EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
