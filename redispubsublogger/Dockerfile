FROM maven:3.6.2-jdk-8

WORKDIR /code

COPY ./ /code

RUN mvn package

ENTRYPOINT [ "java", "-jar", "target/redis-pubsub-logger-1.0-SNAPSHOT.jar" ]