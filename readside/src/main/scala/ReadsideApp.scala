import Main.xa
import cats.effect.{ExitCode, IO, IOApp}
import fs2.kafka.{AutoOffsetReset, ConsumerSettings, Deserializer}
import org.apache.kafka.common.serialization.StringDeserializer
import pub_sub.algebra.KafkaKeyValueLike.KafkaKeyValue
import pub_sub.interpreter.fs2.MessageProcessor.fs2MessageProcessor
import pub_sub.interpreter.fs2.MessageBrokerRequirements.productionSettings
import cats.effect._
import cats.implicits._
import com.typesafe.config.{Config, ConfigFactory}
import doobie.{Transactor, Update0}
import doobie.util.ExecutionContexts
import fs2.kafka._

import scala.concurrent.duration._
import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts
import cats._
import cats.data._
import cats.effect._
import cats.implicits._
import com.typesafe.config.{Config, ConfigFactory}
import entities.marshalling._
import entities.{CountryGrossDomesticProductGlobalRanking, CountryYearlyTotalPopulation}

object ReadsideApp extends IOApp {

  implicit val cs = IO.contextShift(ExecutionContexts.synchronous)

  object infrastructure {

    lazy val setup: Config = ConfigFactory.load().getConfig("postgres.setup")

    implicit lazy val tr: Transactor[IO] = {

      val host = setup.getString("host")

      val port = setup.getString("port")

      val user = setup.getString("user")

      val pass = setup.getString("pass")

      Transactor.fromDriverManager[IO](
        "org.postgresql.Driver",
        s"jdbc:postgresql://$host:$port/postgres",
        user,
        pass
      )

    }

  }

  val xa: doobie.Transactor[IO] = infrastructure.tr

  override def run(args: List[String]): IO[ExitCode] = {

    val c: ConsumerSettings[IO, String, String] = ConsumerSettings(
      keyDeserializer = Deserializer.delegate[IO, String](new StringDeserializer),
      valueDeserializer = Deserializer.delegate[IO, String](new StringDeserializer)
    ).withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers("localhost:9092")
      .withGroupId("group")

    val readsideMessageProcessor =
      fs2MessageProcessor(
        productionSettings.copy(
          consumerSettings = c
        )
      )(
        "readside"
      )

    readsideMessageProcessor(
      "AddedGDP"
    )({ case KafkaKeyValue(key, json) =>
      import serialization.decode

      decode[CountryGrossDomesticProductGlobalRanking](json) match {
        case Left(value) =>
          Left(value)
        case Right(CountryGrossDomesticProductGlobalRanking(country, globalRanking)) =>
          sql"insert into country_gdp_ranking (countryCode, rank) values ($country, ${globalRanking})".update.run
            .transact(xa)
            .unsafeRunSync
          Right(IO.pure((key, json)))
      }
    })

    readsideMessageProcessor(
      "AddedGDP"
    )({ case KafkaKeyValue(key, json) =>
      import serialization.decode

      decode[CountryYearlyTotalPopulation](json) match {
        case Left(value) =>
          Left(value)
        case Right(CountryYearlyTotalPopulation(country, year, population)) =>
          sql"insert into country_yearly_population_delta (countryCode, year, yearly_population_delta) values ($country, $year, $population)".update.run
            .transact(xa)
            .unsafeRunSync
          Right(IO.pure((key, json)))
      }
    })

  }
}
