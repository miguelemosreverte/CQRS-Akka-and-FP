package pub_sub.interpreter.fs2

import cats.effect._
import cats.implicits._
import fs2.{Pipe, Stream}
import fs2.Chunk
import pub_sub.algebra.KafkaKeyValueLike
import cats.effect.{ExitCode, IO}
import fs2.kafka.{
  produce,
  AutoOffsetReset,
  ConsumerSettings,
  Deserializer,
  ProducerRecord,
  ProducerRecords,
  ProducerResult,
  ProducerSettings
}

object MessageProducer {

  type Topic = String
  val fs2MessageProducer: MessageBrokerRequirements => pub_sub.algebra.MessageProducer.MessageProducer[IO[ExitCode]] =
    requirements =>
      data =>
        topic =>
          handler => {
            implicit val contextShift: ContextShift[IO] = requirements.contextShift
            val stream = Stream.chunk(Chunk.array(data.toArray))
            def publishMessages: PublishPipe =
              _.map(keyValue => ProducerRecords.one(ProducerRecord(topic, keyValue.key, keyValue.value)))
                .through(produce(requirements.transactionalProducerSettings.producerSettings))

            (stream through publishMessages).compile.drain.as(ExitCode.Success)
          }

  type PublishResult = ProducerResult[String, String, Unit]
  type PublishPipe = Pipe[IO, KafkaKeyValueLike, PublishResult]

}
