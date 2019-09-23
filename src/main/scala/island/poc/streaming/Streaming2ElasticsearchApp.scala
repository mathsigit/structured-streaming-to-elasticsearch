package island.poc.streaming

import java.io.{ByteArrayOutputStream, File, FileInputStream}
import java.text.SimpleDateFormat

import org.apache.spark.sql.SparkSession
import org.elasticsearch.spark.sql._

object Streaming2ElasticsearchApp {

  var targetFileList = Array.empty[File]

  def main(args: Array[String]): Unit = {
//    targetFileList = cleanFileList(new File("/Volumes/Sdhd/Downloads/Q2_ELK/ptt_corpus_tokenize"))
    targetFileList = cleanFileList(new File("/Users/stana/Downloads/tsb-poc/Q2_ELK/ptt_corpus_tokenize"))

    while(!targetFileList.isEmpty){
      val spark = SparkSession
        .builder()
        .appName("WriteToES")
        .master("local[*]")
//        .config("spark.es.nodes","localhost")
        .config("spark.es.nodes","10.3.0.36")
        .config("spark.es.port","9200")
        .config("es.index.auto.create", "true")
        .config("es.nodes.wan.only", "true")
        .getOrCreate()
      import spark.implicits._
      val indexDocuments = Streaming2ElasticsearchApp.genEsDataSeq().toDF()
//      indexDocuments.saveToEs("stt_corpus")
      indexDocuments.saveToEs("test_stt_corpus")


      targetFileList = targetFileList.drop(getTargetFileListDropSize(targetFileList))
    }
  }

  def getListOfFiles(dir: File): Array[File] = {
    val these = dir.listFiles
    these ++ these.filter(_.isDirectory).flatMap(getListOfFiles).toList
  }

  def cleanFileList(dir: File): Array[File] = {
    val files = getListOfFiles(dir).filter{
      f => f.isFile && f.getName.endsWith(".txt")
    }
    files
  }

  def getTargetFileListDropSize(list: Array[File]): Int ={
    val takeLength = {
      if (list.length >= 100) 100
      else list.length
    }
    takeLength
  }

  def genEsDataSeq(): Seq[EsDataIndex] ={
    val f100 = targetFileList.take(getTargetFileListDropSize(targetFileList))
    var ns = Seq.empty[EsDataIndex]
    for ((x,i) <- f100.zipWithIndex) {
      ns = genEsData(x,ns)
    }
    ns
  }

  def genEsData(f: File, fs: Seq[EsDataIndex]): Seq[EsDataIndex] ={
    val ff = f
    val dateOfFile = f.getName.split("\\.").toList(1)
    val EData = EsDataIndex(
      (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(dateOfFile.toLong).replace(" ","T"),
      f.getName,
      fileToString(f)
    )
    if(fs.isEmpty){
      Seq(EData)
    }
    else{
      fs :+ EData
    }
  }

  def fileToString(file: File): String = {
    val inStream = new FileInputStream(file)
    val outStream = new ByteArrayOutputStream
    try {
      var reading = true
      while (reading) {
        inStream.read() match {
          case -1 => reading = false
          case c => outStream.write(c)
        }
      }
      outStream.flush()
    }
    finally {
      inStream.close()
    }
    new String(outStream.toByteArray()).filter(_ >= ' ')
  }
}

case class EsDataIndex(
                        call_date:String,
                        call_id:String,
                        conversation: String
                      )
