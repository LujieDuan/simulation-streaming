package io.lujieduan.benchmark.kafka.producer

import com.typesafe.config.ConfigFactory


// Reference:
// https://medium.com/@itseranga/kafka-producer-with-scala-and-akka-a66d6e132e89


trait KafkaConf {

  // config object
  val conf = ConfigFactory.load("kafka")

  // kafka config
  lazy val kafkaHost = conf.getString("kafka.host")
  lazy val kafkaPort = conf.getInt("kafka.port")
  lazy val kafkaTopic = conf.getString("kafka.topic")

}
