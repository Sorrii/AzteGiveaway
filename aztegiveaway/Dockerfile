# Stage 1: Build the application
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the final image
FROM openjdk:17-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar aztegiveaway.jar
ENTRYPOINT ["java", "-jar", "aztegiveaway.jar"]
