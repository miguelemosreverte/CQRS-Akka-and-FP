package use_cases.highest_growing_countries_ranked_by_gdp.stage_1

import akka.actor.ActorSystem
import akka.http.AkkaHttpClient
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.unmarshalling.Unmarshal
import cats.effect.{ExitCode, IO, IOApp}
import country_yearly_population_delta.application.commands.CountryCommands.AddYearlyCountryPopulation
import entities.CountryYearlyTotalPopulation
import org.slf4j.LoggerFactory
import play.api.libs.json._
import pub_sub.algebra.KafkaKeyValueLike.KafkaKeyValue
import pub_sub.algebra.MessageProducer.ProducedNotification
import pub_sub.interpreter.fs2.MessageBrokerRequirements

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.util.Try

object PopulationGet extends IOApp {

  case class CountryWithNullablePopulation(countryiso3code: Option[String], date: Option[String], value: Option[Double])
  case class Country(countryiso3code: String, year: String, value: Int)

  implicit val CountryF = Json.format[Country]
  implicit val CountryReadsF = Reads.seq(Json.reads[Country])
  implicit val CountryWithNullablePopulationF = Json.format[CountryWithNullablePopulation]
  implicit val CountryWithNullablePopulationReadsF = Reads.seq(Json.reads[CountryWithNullablePopulation])

  implicit val actorSystem = ActorSystem("ActorSystem")
  implicit val ec = actorSystem.dispatcher
  val httpClient = new AkkaHttpClient()
  val log = LoggerFactory.getLogger(this.getClass)

  override def run(args: List[String]): IO[ExitCode] = {
    val populationFuture = for {
      response: HttpResponse <- httpClient.get(
        "https://api.worldbank.org/v2/en/country/all/indicator/SP.POP.TOTL?format=json&per_page=20000&source=2&page=1"
      )
      text: String <- Unmarshal(response.entity).to[String]
    } yield {
      val json: JsValue = Json.parse(text)
      val transform: JsLookupResult = json.\(1)
      val result = transform match {
        case JsDefined(value: JsValue) =>
          serialization.decode[Seq[CountryWithNullablePopulation]](value.toString())
        case _: JsUndefined =>
          Left(new ArrayIndexOutOfBoundsException("DataBank did not provide the data correctly."))
      }
      result.map { result: Seq[CountryWithNullablePopulation] =>
        result.flatMap { result =>
          Try {
            Country(result.countryiso3code.get, result.date.get, result.value.get.toInt)
          }.toOption // throwing away all events that do not suffice the requirements #cleanup-stage
        }

      } match {
        case Right(v) => v
        case Left(value) =>
          log.error(value.getMessage)
          Seq[Country]()
      }
    }

    // NOT PRODUCTION CODE, thus we are safe using blocking calls
    val population = Await.result(populationFuture, 60.seconds)

    import entities.marshalling._

    pub_sub.interpreter.fs2.MessageProducer
      .fs2MessageProducer(MessageBrokerRequirements.productionSettings)(
        ProducedNotification.print(ProducedNotification.producedNotificationStandardPrintFormat)
      )(
        CountryYearlyTotalPopulation.name
      )(
        population
          .map { toDomain =>
            CountryYearlyTotalPopulation(
              toDomain.countryiso3code,
              toDomain.year.toInt,
              toDomain.value
            )
          }
          .map { countryPopulation =>
            KafkaKeyValue(
              key = countryPopulation.country,
              value = serialization.encode(
                countryPopulation
              )
            )
          }
      )
  }
}
