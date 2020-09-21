package pub_sub.interpreter.alpakka

import akka.Done
import akka.stream.UniqueKillSwitch
import pub_sub.interpreter.alpakka.MessageProcessor.alpakkaMessageProcessor
import pub_sub.interpreter.alpakka.MessageProducer.alpakkaMessageProducer
import pub_sub.interpreter.utils.KafkaMessageBrokerRequirements

import scala.concurrent.Future

object PubSub {
  val PubSubAlpakka: pub_sub.algebra.PubSub[KafkaMessageBrokerRequirements, UniqueKillSwitch, Future[Done]] =
    pub_sub.algebra.PubSub(
      messageProcessor = alpakkaMessageProcessor,
      messageProducer = alpakkaMessageProducer
    )
}
