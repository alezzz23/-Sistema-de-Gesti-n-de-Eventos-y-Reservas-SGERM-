# Usar una imagen base con OpenJDK 17
FROM openjdk:17-jdk-slim

# Establecer el directorio de trabajo
WORKDIR /app

# Copiar el archivo pom.xml y descargar dependencias
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Dar permisos de ejecución al wrapper de Maven
RUN chmod +x ./mvnw

# Descargar dependencias (esto se cachea si el pom.xml no cambia)
RUN ./mvnw dependency:go-offline -B

# Copiar el código fuente
COPY src ./src

# Compilar la aplicación
RUN ./mvnw clean package -DskipTests

# Exponer el puerto 8080
EXPOSE 8080

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "target/event-management-system-1.0.0.jar"]