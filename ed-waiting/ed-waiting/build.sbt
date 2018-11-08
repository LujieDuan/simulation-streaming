name := "ed-waiting"

version := "0.1"

scalaVersion := "2.12.7"


// https://mvnrepository.com/artifact/org.apache.spark/spark-sql
libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.4.0"

// https://mvnrepository.com/artifact/org.apache.spark/spark-core
libraryDependencies += "org.apache.spark" %% "spark-core" % "2.4.0"


libraryDependencies ++= Seq(
  "net.debasishg" %% "redisclient" % "3.8"
)