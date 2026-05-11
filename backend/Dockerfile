# Build stage — Java 25 avec Maven
FROM eclipse-temurin:25-jdk AS build
RUN apt-get update && apt-get install -y maven --no-install-recommends && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

# Run stage
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]