# Stage 1: build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -q -DskipTests

# Stage 2: runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/frontend.jar app.jar
COPY src/main/resources/static ./static
COPY docker-entrypoint.sh ./docker-entrypoint.sh
# Normalizza i fine riga: se il file viene checkout-ato su Windows con CRLF,
# lo shebang diventa "#!/bin/sh\r" e l'exec fallisce con "no such file or directory".
RUN sed -i 's/\r$//' docker-entrypoint.sh && chmod +x docker-entrypoint.sh
EXPOSE 8081
ENTRYPOINT ["./docker-entrypoint.sh"]
