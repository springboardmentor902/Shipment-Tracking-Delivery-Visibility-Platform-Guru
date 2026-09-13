# Build stage
FROM maven:3.9.9-eclipse-temurin-22 AS build

WORKDIR /build

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY src src

RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:22-jre

WORKDIR /app

COPY --from=build /build/target/*.jar app.jar

EXPOSE 10000

ENTRYPOINT ["java", "-jar", "app.jar"]