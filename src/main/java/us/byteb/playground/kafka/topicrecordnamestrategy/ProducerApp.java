package us.byteb.playground.kafka.topicrecordnamestrategy;

import static java.text.MessageFormat.format;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
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
    props.setProperty("bootstrap.servers", "localhost:9092");
    props.put("key.serializer", StringSerializer.class.getName());
    props.put("value.serializer", KafkaAvroSerializer.class.getName());
    props.put("schema.registry.url", "http://localhost:8081");
    props.put("value.subject.name.strategy", io.confluent.kafka.serializers.subject.TopicRecordNameStrategy.class.getName());

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
