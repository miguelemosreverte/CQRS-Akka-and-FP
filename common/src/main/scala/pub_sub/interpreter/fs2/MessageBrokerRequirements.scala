package pub_sub.interpreter.fs2

import cats.effect.{ContextShift, ExitCode, IO, Timer}
import fs2.kafka.{AutoOffsetReset, ConsumerSettings, Deserializer, ProducerSettings, TransactionalProducerSettings}

case class MessageBrokerRequirements(
    consumerSettings: ConsumerSettings[IO, String, String],
    transactionalProducerSettings: TransactionalProducerSettings[IO, String, String],
    contextShift: ContextShift[IO],
    timer: Timer[IO]
)
object MessageBrokerRequirements {
  def create(
      consumerSettings: ConsumerSettings[IO, String, String],
      producerSettings: TransactionalProducerSettings[IO, String, String]
  )(
      implicit
      contextShift: ContextShift[IO],
      timer: Timer[IO]
  ): MessageBrokerRequirements =
    MessageBrokerRequirements(
      consumerSettings,
      producerSettings,
      contextShift,
      timer
    )

  def productionSettings(
      implicit
      contextShift: ContextShift[IO],
      timer: Timer[IO]
  ): MessageBrokerRequirements = {

    val consumerSettings =
      ConsumerSettings(
        keyDeserializer = Deserializer[IO, String],
        valueDeserializer = Deserializer[IO, String]
      ).withAutoOffsetReset(AutoOffsetReset.Earliest)
        .withBootstrapServers("localhost:9092")
        .withGroupId("group")

    val producerSettings =
      TransactionalProducerSettings(
        "transactional-id",
        ProducerSettings[IO, String, String]
          .withBootstrapServers("localhost:9092")
      )
    MessageBrokerRequirements.create(
      consumerSettings,
      producerSettings
    )
  }

}
