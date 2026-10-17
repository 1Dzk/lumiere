# syntax=docker/dockerfile:1
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
RUN apk add --no-cache bash curl

COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2,id=m2-lumiere-gateway ./mvnw -q -DskipTests dependency:go-offline

COPY src src
RUN --mount=type=cache,target=/root/.m2,id=m2-lumiere-gateway ./mvnw -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine AS layers
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S lumiere && adduser -S lumiere -G lumiere
COPY --from=layers /app/app/dependencies/ ./
COPY --from=layers /app/app/spring-boot-loader/ ./
COPY --from=layers /app/app/snapshot-dependencies/ ./
COPY --from=layers /app/app/application/ ./
USER lumiere
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
