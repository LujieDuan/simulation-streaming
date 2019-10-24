# simulation-streaming




## Project: redispubsublogger

### Run
- Run ```mvn package```
- Run the executable with ```java -jar target/redis-pubsub-logger-1.0-SNAPSHOT.jar```

## Project: ed-waiting

### Run
- First ```cd ed-waiting\ed-waiting && mkdir -p waitingtimelist```
- Run ```sbt assembly```;
- Run the executable with ```java -jar target/scala-2.12/ed-waiting-assembly-0.1.jar```;
- Need to use Java 8;


## Project: webviz

### Run
- First ```pip install -r requirements.txt```
- Then ```python webviz.py```
- Access the website from ```localhost:8050```