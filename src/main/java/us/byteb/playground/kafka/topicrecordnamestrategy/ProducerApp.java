package us.byteb.playground.kafka.topicrecordnamestrategy;

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.VALUE_SUBJECT_NAME_STRATEGY;
import static java.text.MessageFormat.format;
import static org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;
import static us.byteb.playground.kafka.topicrecordnamestrategy.Config.BOOTSTRAP_SERVERS;
import static us.byteb.playground.kafka.topicrecordnamestrategy.Config.SCHEMA_REGISTRY_URL;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.subject.TopicRecordNameStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import us.byteb.avro.CookieBaked;
import us.byteb.avro.CookieEaten;

public class ProducerApp {

  public static void main(String[] args) throws InterruptedException {
    Properties props = new Properties();
    props.setProperty(BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
    props.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
    props.put(SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_REGISTRY_URL);
    props.put(VALUE_SUBJECT_NAME_STRATEGY, TopicRecordNameStrategy.class.getName());

    final Random random = new Random();

    int counter = 0;
    final List<Integer> cookieIdList = new ArrayList<>();

    try (KafkaProducer<String, SpecificRecord> producer = new KafkaProducer<>(props)) {

      while (true) {
        final long interval = (long) (5L * Math.random());
        Thread.sleep(interval * 1000);

        if (!cookieIdList.isEmpty() && random.nextBoolean()) {
          final Integer eatingCookieId = cookieIdList.get(0);
          cookieIdList.remove(0);

          System.out.println(format("ate cookie {0}", eatingCookieId));

          final CookieEaten cookieEaten = buildCookieEaten(eatingCookieId);
          producer.send(new ProducerRecord<>(
              "cookies",
              String.valueOf(eatingCookieId), cookieEaten));

        } else {

          final int bakingCookieId = counter++;
          cookieIdList.add(bakingCookieId);

          System.out.println(format("baked cookie {0}", counter));

          producer.send(new ProducerRecord<>(
              "cookies",
              String.valueOf(counter),
              buildCookieBaked(String.valueOf(bakingCookieId))));
        }
      }
    }
  }

  private static CookieEaten buildCookieEaten(final Integer cookieId) {
    return CookieEaten.newBuilder()
        .setCookieEater("eater")
        .setCookieId(String.valueOf(cookieId))
        .build();
  }

  private static CookieBaked buildCookieBaked(final String cookieId) {
    return us.byteb.avro.CookieBaked.newBuilder()
        .setCookieBaker("baker")
        .setCookieId(cookieId)
        .build();
  }

}
