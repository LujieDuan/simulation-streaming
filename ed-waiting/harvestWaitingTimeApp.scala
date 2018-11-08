import scala.io.Source
import org.apache.spark.sql._
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.DataTypes
import org.apache.spark.sql.types.StructField
import org.apache.spark.sql.types.StructType
import org.apache.spark.rdd._
import java.util.Calendar
import org.apache.spark.sql.functions.lit

object SparkPseudoStreamingAndPersisting extends App {
    val sparkSession =   SparkSession
                        .builder()
                        .appName("waitingTimeHTML")
                        .getOrCreate

    import sparkSession.implicits._

    val strHarvesterVersion = "4" 
    val countMinutesBetweenHarvests = 15    
    val countSecondsBetweenHarvests = 60 * countMinutesBetweenHarvests

    while (true)
    {
        val html = Source.fromURL("https://www.saskatoonhealthregion.ca/pages/emergency-wait-times.aspx", "ISO-8859-1").getLines.toStream
        val pattern = Vector(raw"<td>([0-9]+):([0-9]+)</td>".r, raw"<td>([0-9]+)</td>".r, raw"<td>Closed</td>".r, raw"<td></td>".r, raw"<td>Closing Soon</td>".r)
        val harvestDataRaw = for {
            word <- html
            regEx <- pattern
            matches <- regEx.findAllIn(word)
        } yield word

        val harvestData = harvestDataRaw.map(_ replaceAll (" ", "") replaceAll ("[<td>]", "") replaceAll ("/", "")).toVector

        if (harvestData.size == 0)
        {
            print("harvestData vector is: " + harvestData)
        }
        else
        {
        val theRowUniversityV =Vector("Royal University Hospital", harvestData(0), harvestData(1), harvestData(2))
        val theRowCityV =Vector("Saskatoon City Hospital", harvestData(3), harvestData(4), harvestData(5))
        val theRowStPaulV =Vector("St Paul's Hospital", harvestData(6), harvestData(7), harvestData(8))

        val vec = Vector(theRowUniversityV, theRowCityV, theRowStPaulV)

        val harvestrdd = sparkSession.sparkContext.parallelize(vec).map(Row.fromSeq(_))

        val schema = StructType(
            Array(
            StructField("Location", DataTypes.StringType), 
            StructField("Patients Waiting", DataTypes.StringType), 
            StructField("Average Wait (HH:MM)", DataTypes.StringType), 
            StructField("Longest Wait (HH:MM)", DataTypes.StringType))
            )

        val harvestDF = sparkSession.createDataFrame(harvestrdd, schema)

        val now = Calendar.getInstance().getTime().toString

        val harvestWithDateDF = harvestDF.withColumn("Harvest Time", lit(now))

        println(s"waiting list was harvested at $now")
    
        harvestWithDateDF.show

        harvestWithDateDF.select("Location","Patients Waiting", "Average Wait (HH:MM)", "Longest Wait (HH:MM)", "Harvest Time").repartition(1).write.mode(org.apache.spark.sql.SaveMode.Append).csv(s"waitingtimelist/$strHarvesterVersion")
        }

        Thread.sleep(countSecondsBetweenHarvests * 1000)
    }
}