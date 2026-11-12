# Imagen de Mira (D28). Dos etapas: la primera compila con Maven y la segunda solo ejecuta el
# .jar, sin Maven ni el código fuente. Se construye con `docker compose up --build`.

# --- Etapa 1: construcción -------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS construccion
WORKDIR /fuente

# Primero solo el pom: las dependencias quedan en una capa propia y no se vuelven a
# descargar mientras el pom no cambie.
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

COPY src ./src
# Las pruebas se ejecutan antes, con ./mvnw clean verify (regla 5); acá solo se empaqueta.
RUN mvn -q -B package -DskipTests

# --- Etapa 2: ejecución ----------------------------------------------------------------
FROM eclipse-temurin:21-jre
WORKDIR /app

# La aplicación no corre como root.
RUN groupadd --system mira && useradd --system --gid mira --home /app mira \
    && mkdir -p /data/uploads && chown -R mira:mira /data /app
COPY --from=construccion --chown=mira:mira /fuente/target/mira-*.jar /app/mira.jar
USER mira

ENV TZ=America/Santiago \
    APP_UPLOAD_DIR=/data/uploads
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/mira.jar"]
