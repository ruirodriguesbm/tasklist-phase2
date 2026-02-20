# ==========================================
# Stage 1: Build
# ==========================================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -q -B -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -B -DskipTests clean package

# ==========================================
# Stage 2: Runtime
# ==========================================
FROM eclipse-temurin:21-jre

LABEL description="TodoList REST API"
LABEL java.version="21"

WORKDIR /app

# Security: non-root user
RUN groupadd -r javalin && useradd -r -g javalin -u 10001 -m javalin
# Copiar o "fat jar" (o maior / o que interessa)
# 1) copia todos para /tmp
COPY --from=build /app/target/*.jar /tmp/

# 2) escolhe o jar correto e coloca como app.jar
RUN set -eux; \
    ls -lah /tmp; \
    JAR="$(ls -1S /tmp/*.jar | grep -v 'original-' | head -n 1)"; \
    echo "Using jar: $JAR"; \
    cp "$JAR" /app/app.jar; \
    chown -R javalin:javalin /app

COPY cert.pem /app/cert.pem
COPY key.pem /app/key.pem

USER javalin

EXPOSE 7100
ENTRYPOINT ["java","-jar","/app/app.jar"]