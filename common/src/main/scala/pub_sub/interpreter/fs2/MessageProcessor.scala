package pub_sub.interpreter.fs2

import cats.effect.{ContextShift, ExitCode, IO, Timer}
import fs2.kafka.{
  consumerStream,
  transactionalProducerStream,
  CommittableConsumerRecord,
  CommittableProducerRecords,
  ConsumerSettings,
  ProducerRecord,
  TransactionalKafkaProducer,
  TransactionalProducerRecords,
  TransactionalProducerSettings
}
import pub_sub.algebra.KafkaKeyValue
import pub_sub.algebra.MessageProcessor.MessageProcessor

import scala.concurrent.duration.DurationInt

object MessageProcessor {

  case class FS2MessageProcessorRequirements(
      consumerSettings: ConsumerSettings[IO, String, String],
      producerSettings: TransactionalProducerSettings[IO, String, String],
      contextShift: ContextShift[IO],
      timer: Timer[IO]
  )
  object FS2MessageProcessorRequirements {
    def create(
        consumerSettings: ConsumerSettings[IO, String, String],
        producerSettings: TransactionalProducerSettings[IO, String, String]
    )(
        implicit
        contextShift: ContextShift[IO],
        timer: Timer[IO]
    ): FS2MessageProcessorRequirements =
      FS2MessageProcessorRequirements(
        consumerSettings,
        producerSettings,
        contextShift,
        timer
      )
  }

  type MessageProcessorOutput = IO[ExitCode]
  type AlgorithmOutput = IO[(String, String)]
  type Algorithm = pub_sub.algebra.MessageProcessor.Algorithm[AlgorithmOutput]

  val fs2MessageProcessor: FS2MessageProcessorRequirements => MessageProcessor[
    MessageProcessorOutput,
    AlgorithmOutput
  ] =
    transactionRequirements =>
      consumerGroup =>
        topicName =>
          algorithm => {
            implicit val contextShift: ContextShift[IO] = transactionRequirements.contextShift
            implicit val timer: Timer[IO] = transactionRequirements.timer
            val stream =
              transactionalProducerStream[IO]
                .using(transactionRequirements.producerSettings)
                .flatMap { producer: TransactionalKafkaProducer[IO, String, String] =>
                  consumerStream[IO]
                    .using(transactionRequirements.consumerSettings.withGroupId(consumerGroup))
                    .evalTap(_.subscribeTo(topicName))
                    .flatMap(_.stream)
                    .mapAsync(25) { committable: CommittableConsumerRecord[IO, String, String] =>
                      val key = committable.record.key
                      val value = committable.record.value
                      val kafkaKeyValue = KafkaKeyValue(key, value)
                      algorithm(kafkaKeyValue) match {
                        case Left(exception) =>
                          val record = ProducerRecord(topicName + "_retry", key, value)
                          IO.pure(CommittableProducerRecords.one(record, committable.offset))
                        case Right(v: IO[(String, String)]) =>
                          v.map { _ =>
                            val record = ProducerRecord(topicName + "_sink", key, value)
                            CommittableProducerRecords.one(record, committable.offset)
                          }

                      }
                    }
                    .groupWithin(500, 15.seconds)
                    .map(TransactionalProducerRecords(_))
                    .evalMap(producer.produce)
                }

            import cats.syntax.functor._
            stream.compile.drain.as(ExitCode.Success)
          }

}
