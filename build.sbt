lazy val root = (project in file(".")).
  settings(
    name := "structured-streaming-to-elasticsearch",
    version := "0.1",
    scalaVersion := "2.11.8",
    mainClass in (Compile) := Some("island.poc.streaming.Streaming2ElasticsearchApp"),
    mainClass in (Compile, run) := Some("island.poc.streaming.Streaming2ElasticsearchApp"),
    mainClass in (Compile, packageBin) := Some("island.poc.streaming.Streaming2ElasticsearchApp")
  )

val spark2Ver = "2.2.0"

val typesafeConfig = "com.typesafe" % "config" % "1.3.3"
val spark2Core = "org.apache.spark" %% "spark-core" % spark2Ver % "provided"
val spark2Sql = "org.apache.spark" %% "spark-sql" % spark2Ver % "provided"
val elasticsearchHadoop = "org.elasticsearch" % "elasticsearch-hadoop" % "7.2.1"

libraryDependencies ++= Seq(
  typesafeConfig,
  spark2Core,
  spark2Sql,
  elasticsearchHadoop
)


//Setting for sbt-assembly
assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

assemblyMergeStrategy in assembly := {
  case PathList("org","aopalliance", xs @ _*) => MergeStrategy.last
  case PathList("javax", "inject", xs @ _*) => MergeStrategy.last
  case PathList("javax", "servlet", xs @ _*) => MergeStrategy.last
  case PathList("javax", "activation", xs @ _*) => MergeStrategy.last
  case PathList("org", "apache", xs @ _*) => MergeStrategy.last
  case PathList("com", "google", xs @ _*) => MergeStrategy.last
  case PathList("com", "esotericsoftware", xs @ _*) => MergeStrategy.last
  case PathList("com", "codahale", xs @ _*) => MergeStrategy.last
  case PathList("com", "yammer", xs @ _*) => MergeStrategy.last
  case "about.html" => MergeStrategy.rename
  case "META-INF/ECLIPSEF.RSA" => MergeStrategy.last
  case "META-INF/mailcap" => MergeStrategy.last
  case "META-INF/mimetypes.default" => MergeStrategy.last
  case "plugin.properties" => MergeStrategy.last
  case "log4j.properties" => MergeStrategy.last
  case x if x.endsWith("overview.html") => MergeStrategy.last
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
