package pub_sub.algebra

import scala.concurrent.Future
import akka.Done
import pub_sub.algebra.MessageProducer.ProducedNotification
import pub_sub.algebra.MessageProducer.ProducedNotification.Defaults.{setPrintOption, MessageProducerPrintDefaults}

object MessageProducer {

  type Topic = String
  type MessageProducer[Output] = (ProducedNotification => Unit) => Topic => Seq[KafkaKeyValueLike] => Output

  object Defaults {
    def publishSingleValue[Message <: KafkaKeyValueLike, Output](value: KafkaKeyValueLike)(topic: String)(implicit
        messageProducer: MessageProducer[Output],
        toKafkaValueLike: Message => KafkaKeyValueLike,
        printOption: MessageProducerPrintDefaults
    ) =
      messageProducer(setPrintOption(printOption))(topic)(Seq(value))
  }

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

    object Defaults {

      val silent = noPrint(producedNotificationStandardPrintFormat)
      val defaultPrint = print(producedNotificationStandardPrintFormat)

      sealed trait MessageProducerPrintDefaults
      implicit case object Silent extends MessageProducerPrintDefaults
      implicit case object StandardPrint extends MessageProducerPrintDefaults
      def setPrintOption(option: MessageProducerPrintDefaults): ProducedNotification => Unit = option match {
        case Silent => silent
        case StandardPrint => defaultPrint
      }

    }
  }
}
