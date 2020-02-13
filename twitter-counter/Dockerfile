FROM hseeberger/scala-sbt:8u181_2.12.7_1.2.6

WORKDIR /code

COPY . /code

RUN sbt assembly

ENTRYPOINT ["java", "-jar", "./target/scala-2.12/twitter-counter-assembly-0.1.jar"]