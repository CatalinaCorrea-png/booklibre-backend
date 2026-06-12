# ─────────────────────────────────────────────────────────────────────────────
# Etapa 1: build del fat jar con Gradle 8.10 + JDK 21 (igual que el wrapper local)
# ─────────────────────────────────────────────────────────────────────────────
FROM gradle:8.10-jdk21 AS build
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . .
# -x test: los tests de integración necesitan Postgres/Mongo/Redis, que no existen
# durante el build de Render. bootJar no corre tests, pero lo dejamos explícito.
RUN gradle clean bootJar --no-daemon -x test

# ─────────────────────────────────────────────────────────────────────────────
# Etapa 2: runtime liviano, solo el JRE + el fat jar (imagen final chica)
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/backend-2026-grupo2-1.0-SNAPSHOT.jar app.jar
# Render inyecta la env var PORT; la app la lee con server.port=${PORT:8080}.
# EXPOSE es solo informativo.
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
