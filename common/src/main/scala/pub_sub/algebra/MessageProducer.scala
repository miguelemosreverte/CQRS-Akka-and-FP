package pub_sub.algebra

import scala.concurrent.Future
import akka.Done
import pub_sub.algebra.MessageProducer.ProducedNotification

object MessageProducer {

  type Topic = String
  type MessageProducer[Output] = Seq[KafkaKeyValueLike] => Topic => (ProducedNotification => Unit) => Output

  case class ProducedNotification(topic: String, produced: Seq[KafkaKeyValueLike])
  object ProducedNotification {
    // THIS IS SO HASKELL IS NOT EVEN FUNNY
    type PrintFormat = ProducedNotification => Seq[String]
    type Print = PrintFormat => ProducedNotification => Unit
    def print: Print =
      printFormat => producedNotification => println(printFormat(producedNotification).mkString("\n"))
    def noPrint: Print =
      printFormat => producedNotification => ()

    def producedNotificationStandardPrintFormat: PrintFormat =
      producedNotification =>
        producedNotification.produced map { message =>
          s"""
           |${Console.YELLOW} [MessageProducer] ${Console.RESET}
           |Sending message to: 
           |${Console.YELLOW} [${producedNotification.topic}] ${Console.RESET}
           |${Console.CYAN} ${message.value} ${Console.RESET}
           |""".stripMargin
        }
  }
}
