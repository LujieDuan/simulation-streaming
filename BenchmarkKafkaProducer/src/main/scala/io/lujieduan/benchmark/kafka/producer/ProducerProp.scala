package io.lujieduan.benchmark.kafka.producer

import java.util.Properties
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig}
import org.apache.kafka.common.serialization.StringSerializer

// Reference:
// https://medium.com/@itseranga/kafka-producer-with-scala-and-akka-a66d6e132e89



trait ProducerProp extends KafkaConf {

  lazy val props = new Properties()
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, s"$kafkaHost:$kafkaPort")
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getCanonicalName)
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getCanonicalName)
  props.put(ProducerConfig.RETRIES_CONFIG, "5")

  lazy val producer = new KafkaProducer[String, String](props)
}
