package pub_sub.interpreter

import akka.Done
import scala.concurrent.Future
import akka.stream.UniqueKillSwitch
import pub_sub.algebra.{KafkaKeyValue, KafkaKeyValueLike}
import pub_sub.algebra.MessageProducer.ProducedNotification

class KafkaMock() {

  object PubSub {
    sealed trait PubSubProtocol
    case class Message(topic: String, message: String) extends PubSubProtocol
    case class SubscribeMe(topic: String, algorithm: KafkaKeyValue => Either[Throwable, Future[Done]])
        extends PubSubProtocol
  }
  import PubSub._
  var subscriptors: Set[SubscribeMe] = Set.empty
  var messageHistory: Seq[(String, String)] = Seq.empty

  def receive(message: PubSubProtocol): Unit = message match {
    case m: Message =>
      messageHistory = messageHistory :+ ((m.topic, m.message))
      subscriptors.filter(_.topic == m.topic).foreach {
        _.algorithm(KafkaKeyValue(m.topic, m.message))
      }
    case s: SubscribeMe =>
      subscriptors = subscriptors + s
  }

  def produce(data: Seq[KafkaKeyValueLike], topic: String)(handler: ProducedNotification => Unit) = {
    data map { kafkaKeyValue =>
      PubSub.Message(topic, kafkaKeyValue.value)
    } foreach { receive }
    handler(ProducedNotification(topic, data))
    Future.successful(Done)
  }

  type MessageProcessorKillSwitch = UniqueKillSwitch

  val consumerGroup: String = "default"
  val topicName: String = "default"
  def run(topic: String, algorithm: KafkaKeyValue => Either[Throwable, Future[Done]]): Unit = {
    receive(PubSub.SubscribeMe(topic, algorithm))
    ()
  }

}
