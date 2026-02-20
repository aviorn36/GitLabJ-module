FROM openjdk:8-jdk-alpine
MAINTAINER aviorn36@gmail.com
WORKDIR /gitsync/app/data/
COPY /target/GitSync-0.0.1-SNAPSHOT.jar app.jar
CMD java -jar app.jar