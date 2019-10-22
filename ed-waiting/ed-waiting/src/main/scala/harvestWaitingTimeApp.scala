/*
 harvestWaitingTimeApp
 Created By: Xiaoyan Li
 Revision:
  Modified By: Lujie Duan 2018-11-06
  Publish the waiting time to a Redis channel instead of writing to csv files
  Modified By: Lujie Duan 2019-10-22
  Modified as now there are four possible ED rooms displayed on the websites; removed Spark components for stability
 */
import java.io.{File, PrintWriter}
import scala.io.Source
import java.util.Calendar
import com.redis._

object harvestWaitingTimeApp extends App {

  val r = new RedisClient("localhost", 6379)

  def publishToChannel(channel: String, message: String) = {
    r.publish(channel, message)
  }

  while (true)
  {
    val html = Source.fromURL("https://www.saskatoonhealthregion.ca/pages/emergency-wait-times.aspx", "ISO-8859-1").getLines.toStream
    val table = "<table class=\"waittimetable\">((?!<table).)*</table>".r.findFirstIn(html.mkString
                                                                          .replaceAll("\t", "")
                                                                          .replaceAll("\n", "")
                                                                          .replaceAll("\r", ""))
    val lines = scala.xml.XML.loadString(table.get) \ "tbody" \ "tr"
    val hospital_map = Map("JPCH Children's ED" -> "JPCH_Childrens_ED_patient",
                            "Royal University Hospital Adult ED" -> "Royal_University_Hospital_patient",
                            "Saskatoon City Hospital" -> "Saskatoon_City_Hospital_patient",
                            "St Paul's Hospital" -> "St_Pauls_Hospital_patient")

    val now = Calendar.getInstance().getTime().toString
    println(s"waiting list was harvested at $now")

    if (lines.length == 0) println("NO DATE")
    else {
      var result = "LOCATION, COUNT, AVERAGE TIME, LONGEST TIME\n"
      lines.foreach(x => {
        val cells = x \ "td"
        if (cells.length == 4) {
          val cell_values = cells.map(c => c.toString().replaceAll("<td class=\"location\">", "")
                                                            .replaceAll("<td>", "")
                                                            .replaceAll("</td>", "")
                                                            .trim())
          val channel_name = hospital_map(cell_values(0))
          publishToChannel(channel_name, if (cell_values(1) == "") "0.0" else cell_values(1))
          result = result + "\"" + cell_values(0) + "\"," + cell_values(1) + "," + cell_values(2) + "," + cell_values(3) + "\n"
        }
      })
      println(result)
      val pw = new PrintWriter(new File("./waitingtimelist/waiting-time-" + now + ".csv" ))
      pw.write(result)
      pw.close()
    }

    val countMinutesBetweenHarvests = 15
    val countSecondsBetweenHarvests = 60 * countMinutesBetweenHarvests
    Thread.sleep(countSecondsBetweenHarvests * 1000)
  }
}