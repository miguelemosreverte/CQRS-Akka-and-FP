package pub_sub.scaladsl

import org.slf4j.LoggerFactory
import scala.language.implicitConversions
import pub_sub.algebra.MessageProducer.{MessageProducer, ProducedNotification, Topic}
import pub_sub.algebra.{KafkaKeyValueLike, MessageProducer}

object MessageProducerDSL {
  implicit class MessageProducerObjectDSL[Message, Out](o: MessageProducer.type) {

    def alpakka(implicit requirements: pub_sub.interpreter.alpakka.MessageBrokerRequirements) =
      pub_sub.interpreter.alpakka.MessageProducer.alpakkaMessageProducer(requirements)
    def fs2(implicit requirements: pub_sub.interpreter.fs2.MessageBrokerRequirements) =
      pub_sub.interpreter.fs2.MessageProducer.fs2MessageProducer(requirements)

  }

  implicit class MessageProducerDSL[Message, Out](o: MessageProducer[Out])(implicit
      implicitCompanion: MessageProducerDslImplicitCompanion[Message] = MessageProducerDslImplicitCompanion(
        setTopic = (message: Message) => utils.Inference.getSimpleName(message.getClass.getName),
        setHandlers = Seq.empty
      )
  ) {

    private object Helpers {
      lazy val messageProducerName = utils.Inference.getSimpleName(o.getClass.getName)
      lazy val messageProducerLogger = LoggerFactory.getLogger(o.getClass)
      def messageProducerLog: ProducedNotification => String =
        producedNotification => s"[MessageProducer] [$messageProducerName] | Published ${producedNotification}"
      def handlers(p: ProducedNotification): Unit = implicitCompanion.setHandlers.foreach(_.apply(p))

    }
    import Helpers._

    def withTopic: (Message => String) => MessageProducerDSL[Message, Out] = { topic =>
      new MessageProducerDSL[Message, Out](o)(implicitCompanion.copy(setTopic = topic))
    }
    def withHandler: (ProducedNotification => Unit) => MessageProducerDSL[Message, Out] = { handler =>
      new MessageProducerDSL[Message, Out](o)(
        implicitCompanion.copy(setHandlers = implicitCompanion.setHandlers :+ handler)
      )
    }
    def withLogger: MessageProducerDSL[Message, Out] =
      new MessageProducerDSL[Message, Out](o)(
        implicitCompanion.copy(setHandlers = implicitCompanion.setHandlers :+ {
          messageProducerLogger info messageProducerLog(_)
        })
      )

    def produce(m: Seq[KafkaKeyValueLike], topic: String): Out =
      o(handlers)(topic)(m)

    def produce(m: Seq[Message])(implicit
        toKafkaValue: Message => KafkaKeyValueLike
    ): Out =
      o(handlers)(implicitCompanion.setTopic(m.head))(m map (toKafkaValue))

  }

  case class MessageProducerDslImplicitCompanion[Message](
      setTopic: Message => String,
      setHandlers: Seq[ProducedNotification => Unit]
  )
}
