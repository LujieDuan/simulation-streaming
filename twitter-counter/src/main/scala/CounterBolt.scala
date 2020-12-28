import java.util

import org.apache.storm.task.{OutputCollector, TopologyContext}
import org.apache.storm.topology.OutputFieldsDeclarer
import org.apache.storm.topology.base.BaseRichBolt
import org.apache.storm.tuple.{Fields, Tuple, Values}
import twitter4j.Status

class CounterBolt extends BaseRichBolt {

  private val keywordsList: List[String] = List("flu", "influenza", "cold")
  private var _collector: OutputCollector = _

  override def prepare(topoConf: util.Map[String, AnyRef], context: TopologyContext, collector: OutputCollector): Unit = {
    this._collector = collector 
  }

  override def execute(input: Tuple): Unit = {
    val cur: Status = input.getValueByField("Tweet").asInstanceOf[Status]
    val time = input.getValueByField("Timestamp")
    for (keyword <- keywordsList) {
      if (cur.getText().contains(keyword)) {
        _collector.emit(new Values(1: Integer, time))
        return
      }
    }
    _collector.emit(new Values(0: Integer, time))
  }

  override def declareOutputFields(declarer: OutputFieldsDeclarer): Unit = {
    declarer declare new Fields("Count", "Timestamp")
  }
}