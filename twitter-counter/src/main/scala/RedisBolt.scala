import java.io.{File, PrintWriter}
import java.util
import java.util.Calendar

import com.redis.RedisClient
import org.apache.storm.task.{OutputCollector, TopologyContext}
import org.apache.storm.topology.OutputFieldsDeclarer
import org.apache.storm.topology.base.BaseRichBolt
import org.apache.storm.tuple.Tuple

class RedisBolt extends BaseRichBolt {
  private var r: RedisClient = _

  override def prepare(topoConf: util.Map[String, AnyRef], context: TopologyContext, collector: OutputCollector): Unit = {}

  override def execute(input: Tuple): Unit = {
    r = new RedisClient(TwitterCounter.host, TwitterCounter.port)

    println("Connecting to " + TwitterCounter.host + ":" + TwitterCounter.port)

    val now = Calendar.getInstance().getTime().toString
    println(s"Twitter Counter finished at $now")

    val count = input.getIntegerByField("Sum")
    r.incrby("Influenza-twitter-total", count.toLong)
    if (System.currentTimeMillis - r.get("last_publish_time").get.toLong > 1000 * 60) {
      val cur = r.get("Influenza-twitter-total").get.toInt - r.get("last_publish_count").get.toInt
      publishToChannel(r, "Influenza-twitter", s"$cur")
      r.set("last_publish_time", System.currentTimeMillis)
      r.set("last_publish_count", cur)
    }

    val result = s"$count"
    val directory = new File("./results")
    if (!directory.exists()) directory.mkdir()
    val pw = new PrintWriter(new File("./results/result-" + now + ".txt"))
    pw.write(result)
    pw.close()
  }

  private def publishToChannel(r: RedisClient, channel: String, message: String) = {
    r.publish(channel, message)
  }

  override def declareOutputFields(declarer: OutputFieldsDeclarer): Unit = {}
}