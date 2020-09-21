package main
import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.functor._
import fs2.kafka._
import pub_sub.algebra.KafkaKeyValue

import scala.concurrent.duration._

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    def processRecord(record: KafkaKeyValue): IO[(String, String)] =
      IO.pure {
        println(record.key -> record.value)
        record.key -> record.value
      }

    val consumerSettings: ConsumerSettings[IO, String, String] =
      ConsumerSettings[IO, String, String]
        .withIsolationLevel(IsolationLevel.ReadCommitted)
        .withAutoOffsetReset(AutoOffsetReset.Earliest)
        .withBootstrapServers("0.0.0.0:9092")
        .withGroupId("group")

    val producerSettings: TransactionalProducerSettings[IO, String, String] =
      TransactionalProducerSettings(
        "transactional-id",
        ProducerSettings[IO, String, String]
          .withBootstrapServers("0.0.0.0:9092")
          .withRetries(Int.MaxValue)
          .withEnableIdempotence(true)
      )

    val stream =
      transactionalProducerStream[IO]
        .using(producerSettings)
        .flatMap { producer =>
          consumerStream[IO]
            .using(consumerSettings)
            .evalTap(_.subscribeTo("ObjetoSnapshotPersisted"))
            .flatMap(_.stream)
            .mapAsync(25) { committable =>
              val key = committable.record.key
              val value = committable.record.value
              val kafkaKeyValue = KafkaKeyValue(key, value)
              val aaaa: IO[CommittableProducerRecords[IO, String, String]] = processRecord(kafkaKeyValue)
                .map {
                  case (key, value) =>
                    val record = ProducerRecord("ObjetoSnapshotPersisted", key, value)
                    CommittableProducerRecords.one(record, committable.offset)
                }
              aaaa
            }
            .groupWithin(500, 15.seconds)
            .map(TransactionalProducerRecords(_))
            .evalMap(producer.produce)
        }

    stream.compile.drain.as(ExitCode.Success)
  }
}
