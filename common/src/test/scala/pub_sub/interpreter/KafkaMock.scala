package pub_sub.interpreter

import scala.concurrent.{ExecutionContext, Future}
import akka.Done
import akka.actor.ActorSystem
import akka.stream.{OverflowStrategy, UniqueKillSwitch}
import akka.stream.scaladsl.{Sink, Source, SourceQueue}
import proof_of_concept.implementation.infrastructure.consumers.AddGdpTransaction
import pub_sub.algebra.MessageProducer.ProducedNotification
import pub_sub.algebra.{KafkaKeyValue, MessageProcessor, MessageProducer}

import scala.collection.mutable

class KafkaMock() extends MessageProducer with MessageProcessor {

  object PubSub {
    sealed trait PubSubProtocol
    case class Message(topic: String, message: String) extends PubSubProtocol
    case class SubscribeMe(topic: String, algorithm: String => Either[Throwable, Future[Done]]) extends PubSubProtocol
  }
  import PubSub._
  var subscriptors: Set[SubscribeMe] = Set.empty
  var messageHistory: Seq[(String, String)] = Seq.empty

  def receive(message: PubSubProtocol): Unit = message match {
    case m: Message =>
      messageHistory = messageHistory :+ ((m.topic, m.message))
      subscriptors.filter(_.topic == m.topic).foreach {
        _.algorithm(m.message)
      }
    case s: SubscribeMe =>
      subscriptors = subscriptors + s
  }

  override def produce(data: Seq[KafkaKeyValue], topic: String)(handler: ProducedNotification => Unit) = {
    data map { kafkaKeyValue =>
      PubSub.Message(topic, kafkaKeyValue.json)
    } foreach { receive }
    handler(ProducedNotification(topic, data))
    Future.successful(Done)
  }

  override type MessageProcessorKillSwitch = UniqueKillSwitch

  override val consumerGroup: String = "default"
  override val topicName: String = "default"
  override def run(algorithm: String => Either[Throwable, Future[Done]]): (Option[UniqueKillSwitch], Future[Done]) = {
    (None, {
      receive(PubSub.SubscribeMe(AddGdpTransaction.topic, algorithm))
      Future.successful(Done)
    })
  }

}
