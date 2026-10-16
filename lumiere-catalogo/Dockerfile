FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
RUN apk add --no-cache bash curl

COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw
COPY pom.xml .
RUN ./mvnw -q -DskipTests dependency:go-offline

COPY src src
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S lumiere && adduser -S lumiere -G lumiere
COPY --from=build /app/target/*.jar app.jar
USER lumiere
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
