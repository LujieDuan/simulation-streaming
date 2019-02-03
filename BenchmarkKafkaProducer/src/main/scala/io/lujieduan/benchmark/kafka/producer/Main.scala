package io.lujieduan.benchmark.kafka.producer

object Main extends App {

  val p = new Producer
  p.send("test", "Another First Message")

}
