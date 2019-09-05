# Kafka `TopicRecordNameStrategy` proof of concept

This repo contains a proof of concept using the subject name strategy setting `TopicRecordNameStrategy` (see [documentation](https://docs.confluent.io/current/schema-registry/serializer-formatter.html#subject-name-strategy)) in Kafka Java Producer and Consumer API.

## Build

```
mvn clean package
```

## Run

1. Start Kafka Broker, Zookeeper and Schema Registry locally. The quickest way is to use [Landoop/fast-data-dev](https://github.com/Landoop/fast-data-dev): 
    ```shell
    docker run --rm --net=host landoop/fast-data-dev
    ```
2. Start the producer application:
    ```shell
    java -jar ./target/producer-app.jar
    ```
3. Start the consumer application:
    ```shell
    java -jar ./target/consumer-app.jar
    ```