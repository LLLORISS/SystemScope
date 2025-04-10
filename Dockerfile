FROM openjdk:17-jdk
FROM ubuntu:24.04

WORKDIR /app

COPY . /app

RUN apt-get update && apt-get install -y openjdk-17-jdk maven openjfx
RUN apt-get update && apt-get install -y maven

RUN ls -l /app

RUN mvn clean install

CMD ["java", "-jar", "target/SystemScope-1.0-SNAPSHOT-jar-with-dependencies.jar"]