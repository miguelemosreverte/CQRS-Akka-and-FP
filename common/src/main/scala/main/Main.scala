
import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.functor._
import fs2.kafka._
import scala.concurrent.duration._

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    def processRecord(record: ConsumerRecord[String, String]): IO[(String, String)] =
      IO.pure(record.key -> record.value)

    val consumerSettings =
      ConsumerSettings[IO, String, String]
        .withIsolationLevel(IsolationLevel.ReadCommitted)
        .withAutoOffsetReset(AutoOffsetReset.Earliest)
        .withBootstrapServers("localhost:9092")
        .withGroupId("group")

    val producerSettings =
      TransactionalProducerSettings(
        "transactional-id",
        ProducerSettings[IO, String, String]
          .withBootstrapServers("localhost:9092")
      )

    val stream =
      transactionalProducerStream[IO]
        .using(producerSettings)
        .flatMap { producer =>
          consumerStream[IO]
            .using(consumerSettings)
            .evalTap(_.subscribeTo("topic"))
            .flatMap(_.stream)
            .mapAsync(25) { committable =>
              processRecord(committable.record)
                .map { case (key, value) =>
                  val record = ProducerRecord("topic", key, value)
                  CommittableProducerRecords.one(record, committable.offset)
                }
            }
            .groupWithin(500, 15.seconds)
            .map(TransactionalProducerRecords(_))
            .evalMap(producer.produce)
        }

    stream.compile.drain.as(ExitCode.Success)
  }
}