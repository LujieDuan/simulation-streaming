import java.util
import java.util.concurrent.{BlockingQueue, LinkedBlockingQueue}

import org.apache.storm.spout.SpoutOutputCollector
import org.apache.storm.task.TopologyContext
import org.apache.storm.topology.OutputFieldsDeclarer
import org.apache.storm.topology.base.BaseRichSpout
import org.apache.storm.tuple.{Fields, Values}
import twitter4j.{FilterQuery, StallWarning, Status, StatusDeletionNotice, StatusListener, TwitterStream, TwitterStreamFactory}


class TwitterSpout extends BaseRichSpout {
  private val queue: BlockingQueue[Status] = new LinkedBlockingQueue[Status]()
  private val config = new twitter4j.conf.ConfigurationBuilder()
    .setOAuthConsumerKey("VQOOxpUPO0Lsxg0I2e3SQXwB9")
    .setOAuthConsumerSecret("G5VfGTnBAaX3fveg02mx3UlsFtZ9Jko3g0vrZqc0lvFbFlHztg")
    .setOAuthAccessToken("800226306684121088-idLB7NsHQsqpsKiXXTr1hdsVMO9B8Cj")
    .setOAuthAccessTokenSecret("eTxqhqvVUdSDB7jvW7tlEjjb50xKJgxJc8CkSJM1OEfu6")
    .build
  private var _collector: SpoutOutputCollector = _
  private var ts: TwitterStream = _

  override def open(conf: util.Map[String, AnyRef], context: TopologyContext, collector: SpoutOutputCollector): Unit = {
    _collector = collector
    ts = new TwitterStreamFactory(config).getInstance
    ts.addListener(simpleStatusListener)
    // Filter and get only SK tweets
//    ts.filter(new FilterQuery().locations(Array(-110.06, 49.08), Array(-101.97, 59.99)))
    ts.sample()
  }

  private def simpleStatusListener = new StatusListener() {
    def onStatus(status: Status) {
      queue.offer(status)
    }

    def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice) {}

    def onTrackLimitationNotice(numberOfLimitedStatuses: Int) {}

    def onException(ex: Exception) {
      ex.printStackTrace()
    }

    def onScrubGeo(arg0: Long, arg1: Long) {}

    def onStallWarning(warning: StallWarning) {}
  }

  override def nextTuple(): Unit = {
    _collector emit new Values(queue.take(),System.currentTimeMillis: java.lang.Long)
  }

  override def declareOutputFields(declarer: OutputFieldsDeclarer): Unit = {
    declarer declare new Fields("Tweet", "Timestamp")
  }

  override def deactivate(): Unit = {
    ts.cleanUp()
  }

  override def close(): Unit = {
    ts.shutdown()
  }
}