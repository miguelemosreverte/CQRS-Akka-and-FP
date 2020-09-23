import cats.effect.{ExitCode, IO, IOApp}
import pub_sub.algebra.MessageProcessor.MessageProcessor
import org.apache.kafka.common.serialization.StringDeserializer
import fs2.kafka.{AutoOffsetReset, ConsumerSettings, Deserializer}
import pub_sub.interpreter.fs2.MessageBrokerRequirements.productionSettings
import pub_sub.interpreter.fs2.MessageProcessor.{fs2MessageProcessor, AlgorithmOutput, MessageProcessorOutput}

object ReadsideApp extends IOApp {

  val readsideMessageProcessor: MessageProcessor[MessageProcessorOutput, AlgorithmOutput] =
    fs2MessageProcessor(
      productionSettings.copy(
        consumerSettings = ConsumerSettings(
          keyDeserializer = Deserializer.delegate[IO, String](new StringDeserializer),
          valueDeserializer = Deserializer.delegate[IO, String](new StringDeserializer)
        ).withAutoOffsetReset(AutoOffsetReset.Earliest)
          .withBootstrapServers("localhost:9092")
          .withGroupId("group")
      )
    )

  def start(
      readsideMessageProcessor: MessageProcessor[MessageProcessorOutput, AlgorithmOutput]
  )(implicit transactor: doobie.Transactor[IO]) = {
    projection.CountryGrossDomesticProductGlobalRankingProjection.start(readsideMessageProcessor)
    projection.CountryYearlyTotalPopulationDeltaProjection.start(readsideMessageProcessor)
  }

  override def run(args: List[String]): IO[ExitCode] =
    start(readsideMessageProcessor)(doobie.Doobie.getTransactor)
}
