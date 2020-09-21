package pub_sub.interpreter.alpakka

import akka.Done
import akka.stream.UniqueKillSwitch
import pub_sub.interpreter.alpakka.MessageProcessor.alpakkaMessageProcessor
import pub_sub.interpreter.alpakka.MessageProducer.alpakkaMessageProducer

import scala.concurrent.Future

object PubSub {
  val PubSubAlpakka: pub_sub.algebra.PubSub[MessageBrokerRequirements, UniqueKillSwitch, Future[Done]] =
    pub_sub.algebra.PubSub(
      messageProcessor = alpakkaMessageProcessor,
      messageProducer = alpakkaMessageProducer
    )
}
