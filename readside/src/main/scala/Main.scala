import application.events.CountryEvents.AddedGDP
import cats.effect.{ExitCode, IO, IOApp}
import fs2.kafka.{AutoOffsetReset, ConsumerSettings, Deserializer}
import org.apache.kafka.common.serialization.StringDeserializer
import pub_sub.algebra.KafkaKeyValue
import pub_sub.interpreter.fs2.MessageProcessor.fs2MessageProcessor
import pub_sub.interpreter.fs2.MessageBrokerRequirements.productionSettings
import cats.effect._
import cats.implicits._
import fs2.kafka._
import scala.concurrent.duration._

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {

    val c: ConsumerSettings[IO, String, String] = ConsumerSettings(
      keyDeserializer = Deserializer.delegate[IO, String](new StringDeserializer),
      valueDeserializer = Deserializer.delegate[IO, String](new StringDeserializer)
    ).withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers("localhost:9092")
      .withGroupId("group")

    fs2MessageProcessor(
      productionSettings.copy(
        consumerSettings = c
      )
    )(
      "readside"
    )(
      "AddedGDP"
    )({
      case KafkaKeyValue(key, json) =>
        import serialization.decode
        import infrastructure.marshalling._
        println(s"key $key, value $json")
        /*        decode[AddedGDP](json) match {
          case Left(value) =>
            println(s"Failure AT READSIDE -- ${value}")
            Right(IO.pure((key, json)))
          //Left(value)
          case Right(value) => {
            println("SUCCESS AT READSIDE")
            Right(IO.pure((key, json)))
          }]*/
        Right(IO.pure(("key", "json")))

      //}
    })

  }
}
