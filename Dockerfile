# =============================================================================
# Dockerfile multi-stage para TodoList API REST (Spring Boot 4.0.5 + Java 17)
# =============================================================================

# ---------- Etapa 1: build ----------
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /build

# Copiamos primero el wrapper de Maven y el pom para aprovechar la caché de Docker:
# si el pom no cambia, no se re-bajan las dependencias.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Permiso de ejecución por si se construye en Windows
RUN chmod +x mvnw

# Descarga dependencias (capa cacheable)
RUN ./mvnw dependency:go-offline -B

# Copiamos el código fuente y empaquetamos el JAR
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

# ---------- Etapa 2: runtime ----------
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# JAR generado en la etapa builder
COPY --from=builder /build/target/*.jar app.jar

# Puerto que expone Spring Boot
EXPOSE 8080

# Variables por defecto (se sobreescriben desde docker-compose o la plataforma cloud)
ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
