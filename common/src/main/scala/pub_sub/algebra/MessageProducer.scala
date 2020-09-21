package pub_sub.algebra

import scala.concurrent.Future
import akka.Done
import pub_sub.algebra.MessageProducer.ProducedNotification

/*
This mechanism allows the user to publish message to the message bus

It delegates onto the user the serialization of the messages
 */

trait MessageProducer {
  def produce(data: Seq[KafkaKeyValue], topic: String)(handler: ProducedNotification => Unit): Future[Done]
}
object MessageProducer {
  case class ProducedNotification(topic: String, produced: Seq[KafkaKeyValue])

  object ProducedNotification {

    def print(
        format: ProducedNotification => Seq[String] = producedNotificationStandardPrintFormat
    )(producedNotification: ProducedNotification): Unit =
      println(format(producedNotification).mkString("\n"))

    def producedNotificationStandardPrintFormat(producedNotification: ProducedNotification): Seq[String] =
      producedNotification.produced map { message =>
        s"""
           |${Console.YELLOW} [MessageProducer] ${Console.RESET}
           |Sending message to: 
           |${Console.YELLOW} [${producedNotification.topic}] ${Console.RESET}
           |${Console.CYAN} ${message.json} ${Console.RESET}
           |""".stripMargin

      }
  }
}
