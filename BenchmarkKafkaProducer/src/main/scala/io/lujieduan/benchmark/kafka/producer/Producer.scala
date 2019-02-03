package io.lujieduan.benchmark.kafka.producer

import org.apache.kafka.clients.producer.ProducerRecord
import com.typesafe.scalalogging.Logger

// Reference:
// https://medium.com/@itseranga/kafka-producer-with-scala-and-akka-a66d6e132e89



class Producer extends ProducerProp{

  private val logger = Logger(classOf[Producer])

  def send(topic: String, message: String): Unit =
  {
    val record = new ProducerRecord[String, String](topic, message)
    producer.send(record)

    logger.info(s"Send message $message to $topic")
  }
}
