package pub_sub.interpreter.alpakka

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.{ProducerMessage, Subscriptions}
import akka.kafka.scaladsl.Transactional
import akka.stream.{KillSwitches, UniqueKillSwitch}
import akka.stream.scaladsl.{Keep, Sink}
import org.apache.kafka.clients.producer.ProducerRecord
import pub_sub.algebra.KafkaKeyValue
import pub_sub.algebra.MessageProcessor.MessageProcessor
import pub_sub.interpreter.utils.KafkaMessageProcessorRequirements
import scala.concurrent.{ExecutionContext, Future}

object MessageProcessor {
  type MessageProcessorOutput = UniqueKillSwitch
  type AlgorithmOutput = Future[Done]
  type Algorithm = pub_sub.algebra.MessageProcessor.Algorithm[AlgorithmOutput]

  val alpakkaMessageProcessor
      : KafkaMessageProcessorRequirements => MessageProcessor[MessageProcessorOutput, AlgorithmOutput] =
    transactionRequirements =>
      consumerGroup =>
        topicName =>
          algorithm => {
            implicit val system: ActorSystem = transactionRequirements.system
            implicit val ec: ExecutionContext = transactionRequirements.executionContext
            val consumer = transactionRequirements.consumer.withGroupId(consumerGroup)
            val producer = transactionRequirements.producer
            val rebalancerListener = transactionRequirements.rebalancerListener
            val subscription = Subscriptions.topics(topicName).withRebalanceListener(rebalancerListener)

            val stream = Transactional
              .source(consumer, subscription)
              .mapAsync(100) { committable =>
                val key = committable.record.key
                val value = committable.record.value
                val kafkaKeyValue = KafkaKeyValue(key, value)
                (algorithm(kafkaKeyValue) match {
                  case Left(value) => Future.failed(new Exception(value))
                  case Right(value) => value
                }).map {

                    case _: Done =>
                      val record =
                        new ProducerRecord(
                          topicName + "_sink",
                          committable.record.key,
                          committable.record.value
                        )
                      ProducerMessage.single(
                        record,
                        committable.partitionOffset
                      )
                  }
                  .recoverWith {
                    case exception: Throwable =>
                      val record =
                        new ProducerRecord(
                          topicName + "_retry",
                          committable.record.key,
                          committable.record.value
                        )
                      Future.successful(
                        ProducerMessage.single(
                          record,
                          committable.partitionOffset
                        )
                      )

                  }

              }
              .via(Transactional.flow(producer, transactionalId))
              .viaMat(KillSwitches.single)(Keep.right)
              .collect {
                case a: ProducerMessage.Result[_, String, _] =>
                  a.message.record.value()
              }
              .toMat(Sink.ignore)(Keep.both)

            val (killSwitch, done) = stream.run()
            killSwitch
          }

  def transactionalId: String = java.util.UUID.randomUUID().toString

}
