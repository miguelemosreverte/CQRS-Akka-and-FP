package country_yearly_population_delta

import akka.actor.ActorSystem
import akka.http.AkkaHttpClient
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.unmarshalling.Unmarshal
import cats.effect.{ExitCode, IO, IOApp}
import country_yearly_population_delta.application.commands.CountryCommands.AddYearlyCountryPopulation
import play.api.libs.json._
import pub_sub.algebra.KafkaKeyValueLike.KafkaKeyValue
import pub_sub.algebra.MessageProducer.ProducedNotification
import pub_sub.interpreter.fs2.MessageBrokerRequirements

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.util.Try

object PopulationGet extends IOApp {

  case class Country(countryiso3code: String, year: String, value: Long) {

    override def toString: String =
      s"""
         |
         |country: ${countryiso3code}
         |year: ${year}
         |value: ${value}
         |
         |""".stripMargin
  }

  case class CountryWithNullablePopulation(countryiso3code: String, year: String, value: Option[Long])
  implicit val CountryF = Json.format[Country]
  implicit val CountryReadsF = Reads.seq(Json.reads[Country])
  implicit val CountryWithNullablePopulationF = Json.format[CountryWithNullablePopulation]
  implicit val CountryWithNullablePopulationReadsF = Reads.seq(Json.reads[CountryWithNullablePopulation])

  implicit val actorSystem = ActorSystem("ActorSystem")
  implicit val ec = actorSystem.dispatcher
  val httpClient = new AkkaHttpClient()

  override def run(args: List[String]): IO[ExitCode] = {
    val populationFuture = for {
      response: HttpResponse <- httpClient.get(
        "https://api.worldbank.org/v2/en/country/all/indicator/SP.POP.TOTL?format=json&per_page=20000&source=2&page=1"
      )
      text: String <- Unmarshal(response.entity).to[String]
    } yield {

      val json: JsValue = Json.parse(text)
      val transform: JsLookupResult = json.\(1)

      val result: Either[Throwable, Seq[CountryWithNullablePopulation]] = transform match {
        case JsDefined(value: JsValue) =>
          serialization.decode[Seq[CountryWithNullablePopulation]](value.toString)
        case _: JsUndefined =>
          Left(new ArrayIndexOutOfBoundsException("DataBank did not provide the data correctly."))
      }
      result.map { result: Seq[CountryWithNullablePopulation] =>
        result.flatMap { result =>
          Try {
            Country(result.countryiso3code, result.year, result.value.get)
          }.toOption
        }

      } match {
        case Right(v) => v
        case Left(value) => Seq[Country]()
      }

    }
    val population = Await.result(populationFuture, 20.seconds)

    println("---")
    println(population.head)

    import country_yearly_population_delta.infrastructure.marshalling._
    val messages = population.map { countryGDP =>
      KafkaKeyValue(
        countryGDP.countryiso3code,
        serialization.encode(
          AddYearlyCountryPopulation(countryGDP.countryiso3code, countryGDP.year.toInt, countryGDP.value)
        )
      )
    }

    pub_sub.interpreter.fs2.MessageProducer
      .fs2MessageProducer(MessageBrokerRequirements.productionSettings)(
        ProducedNotification.print(ProducedNotification.producedNotificationStandardPrintFormat)
      )(
        "AddYearlyPopulationGrowthTransaction"
      )(messages)
  }
}
