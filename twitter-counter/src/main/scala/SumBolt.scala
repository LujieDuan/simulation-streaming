import java.util

import org.apache.storm.task.{OutputCollector, TopologyContext}
import org.apache.storm.topology.OutputFieldsDeclarer
import org.apache.storm.topology.base.BaseWindowedBolt
import org.apache.storm.tuple.{Fields, Values}
import org.apache.storm.windowing.TupleWindow

class SumBolt extends BaseWindowedBolt {

  private var _collector: OutputCollector = _

  override def execute(inputWindow: TupleWindow): Unit = {
    val tuples = inputWindow.get
    val sum = tuples.stream.mapToInt(tuple => tuple.getIntegerByField("Count")).sum
    _collector.emit(new Values(sum: Integer))
  }

  override def prepare(topoConf: util.Map[String, AnyRef], context: TopologyContext, collector: OutputCollector): Unit = {
    this._collector = collector
  }

  override def declareOutputFields(declarer: OutputFieldsDeclarer): Unit = {
    declarer declare new Fields("Sum")
  }

}
