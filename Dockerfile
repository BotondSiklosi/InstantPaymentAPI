FROM openjdk:21-jdk-slim
MAINTAINER Gabor Botond Siklósi <botisiklosi@gmail.com>
WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN ./mvnw clean install -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/instant-payment-api.jar"]