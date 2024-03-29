package island.poc.streaming

import java.io.{ByteArrayOutputStream, File, FileInputStream}
import java.text.SimpleDateFormat

import org.apache.spark.sql.SparkSession
import org.elasticsearch.spark.sql._

object Streaming2ElasticsearchApp {

  var targetFileList = Array.empty[File]

  def main(args: Array[String]): Unit = {
    if (args.length>0){
      val input = args(0)
      targetFileList = cleanFileList(new File(input))

      while(!targetFileList.isEmpty){

        writeToEs(targetFileList)

        targetFileList = targetFileList.drop(getTargetFileListDropSize(targetFileList))
      }
    } else {
      System.out.println("Please input the path of directory")
    }


  }

  def writeToEs(files: Array[File]): Unit = {
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
    val indexDocuments = Streaming2ElasticsearchApp.genEsDataSeq(files).toDF()
//    indexDocuments.saveToEs("stt_corpus", Map("es.mapping.id" -> "call_id"))
    indexDocuments.saveToEs("stt_corpus")
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

  def genEsDataSeq(files: Array[File]): Seq[EsDataIndex] ={
    val f100 = files.take(getTargetFileListDropSize(files))
    var ns = Seq.empty[EsDataIndex]
    for ((x,i) <- f100.zipWithIndex) {
      ns = genEsData(x,ns)
    }
    ns
  }

  def genEsData(f: File, fs: Seq[EsDataIndex]): Seq[EsDataIndex] ={
    val dateOfFile = f.getName.split("\\.").toList(1)
    val EData = EsDataIndex(
      (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(dateOfFile.toLong * 1000L).replace(" ", "T"),
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
    new String(outStream.toByteArray(), "UTF-8").filter(_ >= ' ')
  }
}

case class EsDataIndex(
                        call_date:String,
                        call_id:String,
                        conversation: String
                      )
