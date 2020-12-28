import com.redis.RedisClient
import org.apache.storm.topology.TopologyBuilder
import org.apache.storm.topology.base.BaseWindowedBolt
import org.apache.storm.{Config, LocalCluster, StormSubmitter}

/**
 * Twitter Counter
 * Create by: Lujie Duan
 * Twitter API: https://developer.twitter.com/en/docs/twitter-api/v1/tweets/filter-realtime/guides/basic-stream-parameters
 * Twitter4J with Scala: https://dzone.com/articles/using-twitter4j-scala-access
 * Java Twitter Storm Processing: https://devs4j.com/2017/07/11/real-time-processing-of-tweets-with-apache-storm/
 * Scala Storm Example: https://dzone.com/articles/wordcount-with-storm-and-scala
 * Storm Window Example: https://www.baeldung.com/apache-storm
 */

object TwitterCounter extends App {

  var host = "localhost"
  var port = 6379

  if (args.length >= 1) host = args(0)
  if (args.length >= 2) port = args(1).toInt
  val r: RedisClient = new RedisClient(host, port)
  r.set("last_publish_time", System.currentTimeMillis)
  r.set("last_publish_count", 0)
  r.set("Influenza-twitter-total", 0)

  val builder = new TopologyBuilder
  val conf = new Config()
  builder.setSpout("twitter_spout", new TwitterSpout, 1)
  builder.setBolt("counter_bolt", new CounterBolt, 1).shuffleGrouping("twitter_spout")
  val sum_bolt = new SumBolt().withTimestampField("Timestamp").withTumblingWindow(BaseWindowedBolt.Duration.seconds(1))
  builder.setBolt("sum_bolt", sum_bolt).shuffleGrouping("counter_bolt")
  builder.setBolt("redis_bolt", new RedisBolt).shuffleGrouping("sum_bolt")
  conf.setDebug(true)
  if (args.length >= 3) {
    conf.setNumWorkers(3)
    StormSubmitter.submitTopology(args(2), conf, builder.createTopology())
  }
  else {
    conf.setMaxTaskParallelism(3)
    val cluster = new LocalCluster
    cluster.submitTopology("Twitter Counter", conf, builder.createTopology())
    Thread.sleep(1000000)
    cluster.shutdown()
  }
}
