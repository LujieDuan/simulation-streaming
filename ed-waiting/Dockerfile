FROM hseeberger/scala-sbt:8u222_1.3.3_2.12.10

WORKDIR /code

COPY ./ed-waiting /code

RUN sbt assembly

ENTRYPOINT ["java", "-jar", "./target/scala-2.12/ed-waiting-assembly-0.1.jar"]