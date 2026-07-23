FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
COPY hs-shared/pom.xml hs-shared/pom.xml
COPY hs-user/pom.xml hs-user/pom.xml
COPY hs-auth/pom.xml hs-auth/pom.xml
COPY hs-notification/pom.xml hs-notification/pom.xml
COPY hs-application/pom.xml hs-application/pom.xml

RUN mvn -pl hs-application -am dependency:go-offline

COPY hs-shared hs-shared
COPY hs-user hs-user
COPY hs-auth hs-auth
COPY hs-notification hs-notification
COPY hs-application hs-application

RUN mvn -pl hs-application -am -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /workspace/hs-application/target/hs-application-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
