################################################################################
# 1) Etapa de build: compilamos con Maven en una imagen ligera de OpenJDK 21
################################################################################
FROM maven:3.8.5-openjdk-21 AS builder

# 1.1 Definimos un directorio de trabajo
WORKDIR /build

# 1.2 Copiamos únicamente los archivos de configuración de Maven para maximizar caché
COPY pom.xml mvnw ./
#COPY .mvn .mvn

# Copia el settings.xml.
COPY settings.xml /root/.m2/settings.xml
RUN chmod 600 /root/.m2/settings.xml

# 1.3 Descargamos las dependencias en offline mode (no recompila si pom.xml no cambia)
RUN chmod +x mvnw
RUN mvn dependency:go-offline -B

# 1.4 Copiamos el código fuente y construimos la aplicación (jar)
COPY src ./src
RUN mvn package

################################################################################
# 2) Etapa de runtime: solo JRE, sin Maven ni compiladores
################################################################################
FROM maven:3.8.5-openjdk-21 AS runtime

# 2.1 Creamos un usuario no-root
RUN groupadd --system spring && useradd --system --gid spring spring

# 2.2 Directorio de la aplicación
WORKDIR /app

# 2.3 Copiamos el jar desde el stage builder
COPY --from=builder /build/target/*.jar app.jar

# 2.4 Ajustamos permisos
RUN chown spring:spring /app/app.jar

# 2.5 Exponemos el puerto por defecto de Spring Boot
EXPOSE 8080

# 2.6 Ejecutamos como usuario no privilegiado
USER spring

# 2.7 Arranque de la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]