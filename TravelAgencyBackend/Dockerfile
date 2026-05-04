FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml ./
COPY src ./src

RUN mvn -DskipTests clean package

FROM eclipse-temurin:21-jre
WORKDIR /app

ENV SERVER_PORT=8090

COPY --from=build /app/target/travelagency-backend.jar /app/app.jar

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "/app/app.jar"]