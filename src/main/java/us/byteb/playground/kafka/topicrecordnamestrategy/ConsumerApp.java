package us.byteb.playground.kafka.topicrecordnamestrategy;

import static java.text.MessageFormat.format;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import us.byteb.avro.CookieBaked;
import us.byteb.avro.CookieEaten;

public class ConsumerApp {

  public static void main(String[] args) throws InterruptedException {
    Properties props = new Properties();
    props.setProperty("bootstrap.servers", "localhost:9092");
    props.put("group.id", "uniqueConsumerGroupName");
    props.put("key.deserializer", StringDeserializer.class.getName());
    props.put("value.deserializer", KafkaAvroDeserializer.class.getName());
    props.put("schema.registry.url", "http://localhost:8081");
    props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
    props.put("value.subject.name.strategy", io.confluent.kafka.serializers.subject.TopicRecordNameStrategy.class.getName());

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
