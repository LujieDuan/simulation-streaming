import java.io.{File, PrintWriter}
import java.util.Calendar

import com.redis.RedisClient
import org.apache.spark
import org.apache.spark.sql.SparkSession

import scala.io.Source
//import scala.sys.process._
import org.apache.spark.sql.types.DataTypes
import org.apache.spark.sql.types.StructField
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.functions.udf
import org.joda.time.Days
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
/**
 * Twitter Counter
 * Create by: Lujie Duan
 * Modified based on Yuan Tian's Zeppelin Notebook on Twitter Counting
 */


case class TweetStatus(userId:Option[Long], userLocation:Option[String], place: Option[String], timeCreated: java.sql.Timestamp, isRetweet:Boolean, tweetLanguage:Option[String], value:String,isUserGeoLocationEnabled:Option[Boolean])

object TwitterCounter extends App{

  var host = "localhost"
  var port  = 6379
  if (args.length >= 1) host = args(0)
  if (args.length >= 2) port = args(1).toInt

  println("Connecting to " + host + ":" + port)

  var r : RedisClient = _

  var error_counter = 0

  def publishToChannel(r: RedisClient, channel: String, message: String) = {
    r.publish(channel, message)
  }


  while (true)
  {
    try
      {
        if (r == null)
          r = new RedisClient(host, port)

//        val cmd = "rsync  -avzh --progress --files-from=<(find /source -mtime -3 -type f -exec basename {} \\;) /source /code"
//        val output = cmd.!!
//        print(output)
        val spark = SparkSession.builder().appName("test").master("local").getOrCreate()

        import spark.implicits._

        val allTweetsCSVFields = Array( StructField("user", DataTypes.StringType),
          StructField("userId", DataTypes.LongType),
          StructField("userLocation", DataTypes.StringType),
          StructField("place", DataTypes.StringType),
          StructField("latitude", DataTypes.DoubleType),
          StructField("longitude", DataTypes.DoubleType),
          StructField("timeCreated", DataTypes.StringType),
          StructField("tweetId", DataTypes.LongType),
          StructField("isRetweet", DataTypes.BooleanType),
          StructField("approximateFavoriteCount", DataTypes.IntegerType),
          StructField("tweetLanguage", DataTypes.StringType),
          StructField("isUserGeoLocationEnabled", DataTypes.BooleanType),
          StructField("value", DataTypes.StringType,false))

        val dfraw = spark.read.format("csv").option("header",true).option("quote", "\"").schema(StructType(allTweetsCSVFields)).load("/code/data/*.csv")
        dfraw.count()
        val df = dfraw.select($"userId",$"userLocation",$"place",$"timeCreated",$"isRetweet",$"tweetLanguage",$"value",$"isUserGeoLocationEnabled")

        //use case class
        val dsTweets = df.as[TweetStatus]

        val excludedLocationMap = Vector(raw"(?i)malta".r, raw"(?i)east\s*anglia".r)
        val excludedLocationUDF = udf((strLocation:String) => excludedLocationMap.filter(keyPhraseRegexp => keyPhraseRegexp.findFirstIn(strLocation).isDefined ).size == 0)

        val dsTweetsf = dsTweets.filter(df.col("value").isNotNull).filter($"tweetLanguage"==="en").filter($"userLocation".isNull || excludedLocationUDF($"userLocation")).distinct.dropDuplicates(Seq("isRetweet", "value"))

        val daysList = getDays(new DateTime("2020-02-09"), new DateTime("2020-02-16"))
        val daysDf = daysList.toList.toDF()
        val daysCounts = dsTweets.groupBy(expr("date(timeCreated)")).count()

        val joinedDaysCounts = daysDf.join(daysCounts, daysDf("value") === daysCounts("date(timeCreated)"), "left_outer").orderBy("value").na.fill(0, Seq("count")).select("value", "count")

        val weeksCounts = joinedDaysCounts.select(unix_timestamp($"value", "yyyy-MM-dd").cast(TimestampType).as("timestamp"), $"count").groupBy(window($"timestamp", "1 week")).agg(sum("count")).orderBy("window")

        val now = Calendar.getInstance().getTime().toString
        println(s"Twitter Counters finished at $now")

        val count = weeksCounts
        publishToChannel(r, "Influenza-twitter", s"$count")
        val result = s"$count"
        println(result)
        var directory = new File("./results")
        if (!directory.exists()) directory.mkdir()
        val pw = new PrintWriter(new File("./results/result-" + now + ".txt" ))
        pw.write(result)
        pw.close()
        val countMinutesBetweenHarvests = 60 * 24 * 7
        val countSecondsBetweenHarvests = 60 * countMinutesBetweenHarvests
        Thread.sleep(countSecondsBetweenHarvests * 1000)
      }
    catch
      {
        case x: Exception => {
          println("Error-" + error_counter + ": " + x.getStackTraceString + x.getMessage)
          error_counter += 1
          Thread.sleep(10000)
        }
      }
  }


  def getDays(from: DateTime, until: DateTime) = {
    val numberOfDays = Days.daysBetween(from, until).getDays()
    val outputFormat = DateTimeFormat.forPattern("yyyy-MM-dd");
    for (f<- 0 to numberOfDays) yield outputFormat.print(from.plusDays(f))
  }
}
