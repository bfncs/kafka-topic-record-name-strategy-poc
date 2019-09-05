package us.byteb.playground.kafka.topicrecordnamestrategy;

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.VALUE_SUBJECT_NAME_STRATEGY;
import static io.confluent.kafka.serializers.KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG;
import static java.text.MessageFormat.format;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static us.byteb.playground.kafka.topicrecordnamestrategy.Config.BOOTSTRAP_SERVERS;
import static us.byteb.playground.kafka.topicrecordnamestrategy.Config.CONSUMER_GROUP_ID;
import static us.byteb.playground.kafka.topicrecordnamestrategy.Config.SCHEMA_REGISTRY_URL;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.subject.TopicRecordNameStrategy;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import us.byteb.avro.CookieBaked;
import us.byteb.avro.CookieEaten;

public class ConsumerApp {

  public static void main(String[] args) throws InterruptedException {
    Properties props = new Properties();
    props.setProperty(BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
    props.put(GROUP_ID_CONFIG, CONSUMER_GROUP_ID);
    props.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
    props.put(SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_REGISTRY_URL);
    props.put(SPECIFIC_AVRO_READER_CONFIG, true);
    props.put(VALUE_SUBJECT_NAME_STRATEGY, TopicRecordNameStrategy.class.getName());

    try (KafkaConsumer<String, SpecificRecord> consumer = new KafkaConsumer<>(props)) {
      consumer.subscribe(Collections.singleton("cookies"));

      while (true) {
        final ConsumerRecords<String, SpecificRecord> records = consumer.poll(Duration.ofSeconds(1));
        records.forEach(record -> {
          final SpecificRecord value = record.value();
          System.out.println(format("got record: {0}", value));

          if (value instanceof CookieEaten) {
            System.out.println(
                format("cookie {0} eaten", ((CookieEaten) value).getCookieId()));
          } else if (value instanceof CookieBaked) {
            System.out.println(format("cookie {0} baked", ((CookieBaked) value).getCookieId())) ;
          } else {
            System.out.println(format("something else happened: {0}", record));
          }
        });
      }
    }
  }

}
