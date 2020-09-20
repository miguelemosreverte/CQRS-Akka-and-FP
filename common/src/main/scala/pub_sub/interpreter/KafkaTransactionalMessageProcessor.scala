package pub_sub.interpreter

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.kafka.scaladsl.Transactional
import akka.kafka.{ConsumerMessage, ProducerMessage, ProducerSettings, Subscriptions}
import akka.stream.{KillSwitches, UniqueKillSwitch}
import akka.stream.scaladsl.{Flow, Keep, Sink}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import pub_sub.algebra.MessageProcessor
import pub_sub.interpreter.utils.KafkaMessageProcessorRequirements
import pub_sub.interpreter.utils.KafkaMessageProcessorRequirements.bootstrapServers

case class KafkaTransactionalMessageProcessor(topicName: String, consumerGroup: String = "default")(
    implicit
    transactionRequirements: KafkaMessageProcessorRequirements
) extends MessageProcessor {
  import pub_sub.interpreter.KafkaTransactionalMessageProcessor.Helpers._
  override type MessageProcessorKillSwitch = akka.stream.UniqueKillSwitch
  implicit val ec: ExecutionContext = transactionRequirements.executionContext

  def run(
      algorithm: Algorithm
  ): (Some[UniqueKillSwitch], Future[Done]) = {

    implicit val system: ActorSystem = transactionRequirements.system
    val consumer = transactionRequirements.consumer
    val producer = transactionRequirements.producer
    val rebalancerListener = transactionRequirements.rebalancerListener
    val subscription = Subscriptions.topics(topicName).withRebalanceListener(rebalancerListener)

    val stream = Transactional
      .source(consumer, subscription)
      .mapAsync(100) { msg =>
        applyAlgorithm(msg.record.value, algorithm) map (msg -> _)
      }
      .map(ProducerMessageMulti(topicName, topicName + "_sink"))
      .via(Transactional.flow(producer, transactionalId))
      .viaMat(KillSwitches.single)(Keep.right)
      .collect {
        case a: ProducerMessage.Result[_, String, _] =>
          a.message.record.value()
      }
      .toMat(Sink.ignore)(Keep.both)

    val (killSwitch, done) = stream.run()

    done.onComplete {
      case Success(_) =>
        killSwitch.shutdown()
      case Failure(ex) =>
        killSwitch.shutdown()
        run(algorithm)
    }
    (Some(killSwitch), done)
  }
}

object KafkaTransactionalMessageProcessor {
  object Helpers {
    type Msg = ConsumerMessage.TransactionalMessage[String, String]
    type Algorithm = String => Either[Throwable, Future[Done]]

    def transactionalId: String = java.util.UUID.randomUUID().toString

    def applyAlgorithm(input: String, algorithm: String => Either[Throwable, Future[Done]])(
        implicit
        ec: ExecutionContext
    ): Future[Either[Throwable, akka.Done]] =
      algorithm(input) match {
        case Left(value) => Future.failed(value)
        case Right(value) => value.map(e => Right(e))
      }

    def ProducerMessageMulti(SOURCE_TOPIC: String, SINK_TOPIC: String)(
        tuple: (Msg, Either[Throwable, Done])
    ): ProducerMessage.Envelope[String, String, ConsumerMessage.PartitionOffset] = {
      val message = tuple._1
      val result = tuple._2
      val topic = result match {
        case Left(value) => s"${SOURCE_TOPIC}_retry"
        case Right(value) => SINK_TOPIC
      }
      ProducerMessage.single(
        new ProducerRecord(topic, message.record.key, message.record.value),
        message.partitionOffset
      )
    }
  }

  object KafkaTransactionalMessageProcessor {

    def productionSettings(topicName: String, consumerGroup: String = "default")(
        implicit
        system: ActorSystem,
        ec: ExecutionContext
    ): KafkaTransactionalMessageProcessor = {
      implicit val kafkaMessageProcessorRequirements: KafkaMessageProcessorRequirements =
        KafkaMessageProcessorRequirements.productionSettings(topicName, consumerGroup)
      new KafkaTransactionalMessageProcessor(topicName, consumerGroup)
    }
  }
}
