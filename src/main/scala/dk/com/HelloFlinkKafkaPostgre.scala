package dk.com

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.flink.connector.base.DeliveryGuarantee
import java.util.Properties
import java.sql.PreparedStatement
import scala.language.reflectiveCalls
import org.apache.kafka.common.utils.Utils

import io.circe.Json
//import io.circe.Json.Null
// Need it for asJson
import io.circe.syntax._
// Need it for io.circe.Encoder[Xxx]
//import io.circe.generic.auto._ 

object HelloFlinkKafkaPostgre {
  val LOG = LoggerFactory.getLogger("HelloFlinkKafkaPostgre")

  def main(args: Array[String]): Unit = {
    LOG.info("Start!")

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    // val text = env.fromElements(
    //   "To be, or not to be,--that is the question:--",
    //   "Whether 'tis nobler in the mind to suffer",
    //   "The slings and arrows of outrageous fortune",
    //   "Or to take arms against a sea of troubles,"
    // )
    val text = env.readTextFile("input")

    val counts = text.flatMap { _.toLowerCase.split("\\W+") }
      .map { (_, 1) }
      .keyBy(0)
      .sum(1)
      .map { RequestConvertor.toRequest[(String, Int)](_).right.get }

    //counts print
    counts.addSink(new KafkaSink[(String, String, String)])

    env.execute("temp")

    LOG.info("Done...")
  }
}

object RequestConvertor {
  val TOPIC = "demo_counter"
  def toRequest[T](rows: T): Either[Exception, (String, String, String)] = {
    Right(toJsons(TOPIC, rows))
  }

  def toJsons[T](topic: String, rows: T): (String, String, String) = {
    // For schema
    val field1 = Json.obj(
      "type" -> "string".asJson,
      "optional" -> true.asJson,
      "field" -> "word".asJson
    )
    val field2 = Json.obj(
      "type" -> "int32".asJson,
      "optional" -> true.asJson,
      "field" -> "counter".asJson
    )
    val fields = (field1, field2).asJson
    val schemaJson = Json.obj("type" -> "struct".asJson, "fields" -> fields.asJson)
    // For payload
    val payloadJson = rows match {
      case row: (String, Int) =>
      Json.obj(
        "word" -> row._1.asJson,
        "counter" -> row._2.asJson
      )
    }
    val key = Utils.toPositive(Utils.murmur2("word".getBytes())).toString
    (topic, key, Json.obj("schema" -> schemaJson, "payload" -> payloadJson).noSpaces)
  }
}

class KafkaSink[T]() extends SinkFunction[T] {
  val LOG = LoggerFactory.getLogger("KafkaSink")
  override def invoke(value: T, context: SinkFunction.Context): Unit = {
    val producer = new KafkaProducer[String, String](getProperties)
    value match {
      case records: (String, String, String) =>
        //records.foreach { case (topic, key, message) =>
        LOG.info(s"topic = ${records._1}, key = ${records._2}, message = ${records._3}")
        // See https://docs.confluent.io/platform/current/schema-registry/fundamentals/serdes-develop/serdes-json.html#json-schema-serializer
        val producerRecord = new ProducerRecord[String, String](
          records._1, records._2, records._2)
        producer.send(producerRecord)
      case _ => throw new IllegalArgumentException("Error")
    }
    producer.close()
  }
  private def getProperties: Properties = {
    val properties = new Properties()
    properties.setProperty("bootstrap.servers", "localhost:9092")
    properties.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    properties.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    properties.setProperty("compression.type", "zstd")
    properties
  }
}
