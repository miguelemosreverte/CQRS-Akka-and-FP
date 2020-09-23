package pub_sub.interpreter.fs2

import cats.effect.{Blocker, ExitCode, IO}
import fs2.{Chunk, Pipe, Stream}
import fs2.kafka.{produce, ProducerRecord, ProducerRecords, ProducerResult, ProducerSettings}
import pub_sub.algebra.MessageProducer.{ProducedNotification, Topic}
import pub_sub.algebra.KafkaKeyValueLike

import cats.data.State
import cats.effect._
import cats.effect.concurrent.Ref
import cats.implicits._
import fs2.kafka._
import fs2.{Pipe, Stream}
import play.api.libs.json.Json
import pub_sub.algebra.KafkaKeyValueLike

object MessageProducer {

  type Topic = String
  val fs2MessageProducer: MessageBrokerRequirements => pub_sub.algebra.MessageProducer.MessageProducer[IO[ExitCode]] =
    requirements =>
      handler =>
        topic =>
          data => {
            implicit val contextShift: ContextShift[IO] = requirements.contextShift
            val stream = Stream.chunk(Chunk.array(data.toArray))
            def publishMessages: PublishPipe =
              messageStream =>
                messageStream through { stream =>
                  stream
                    .map(keyValue => ProducerRecords.one(ProducerRecord(topic, keyValue.key, keyValue.value)))
                    .through(produce(producerSettings))
                }

            (stream through publishMessages).compile.drain.as(ExitCode.Success)
          }

  def producerSettings =
    ProducerSettings[IO, String, String]
      .withBootstrapServers("localhost:9092")

  type PublishResult = ProducerResult[String, String, Unit]
  type PublishPipe = Pipe[IO, KafkaKeyValueLike, PublishResult]

}
