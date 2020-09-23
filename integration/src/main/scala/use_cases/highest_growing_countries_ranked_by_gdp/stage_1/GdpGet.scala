package use_cases.highest_growing_countries_ranked_by_gdp.stage_1

import akka.actor.ActorSystem
import akka.http.AkkaHttpClient
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.unmarshalling.Unmarshal
import cats.effect.{ExitCode, IO, IOApp}
import entities.CountryGrossDomesticProductGlobalRanking
import pub_sub.algebra.KafkaKeyValueLike.KafkaKeyValue
import pub_sub.algebra.MessageProducer.ProducedNotification
import pub_sub.interpreter.fs2.MessageBrokerRequirements

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.util.Try

object GdpGet extends IOApp {

  case class CountryGdp(countryCode: String, countryName: String, ranking: String, money: Int) {
    override def toString: String =
      s"""
         |
         |countryCode: ${countryCode}
         |countryName: ${countryName}
         |ranking: ${ranking}
         |money: ${money}
         |
         |""".stripMargin
  }
  object CountryGdp {

    private def clean(column: String): String = {

      val trimmed = (s: String) => s.trim
      val noCommas = (s: String) => s.replace(",", "")
      val noDoubleQuotes = (s: String) => s.replace(""""""", "")
      val noSpaces = (s: String) => s.replace(" ", "")

      trimmed(noCommas(noDoubleQuotes(noSpaces(column))))
    }

    def apply(countryCode: String, ranking: String, countryName: String, money: String): Option[CountryGdp] = {
      if (
        clean(countryCode).nonEmpty &&
        clean(ranking).nonEmpty &&
        clean(countryName).nonEmpty &&
        clean(money).nonEmpty
      ) {

        Try {
          CountryGdp(countryCode, clean(countryName), ranking, clean(money).toInt)
        }.toOption

      } else None
    }
  }

  def run(args: List[String]): IO[ExitCode] = {
    implicit val actorSystem = ActorSystem("ActorSystem")
    implicit val ec = actorSystem.dispatcher
    val httpClient = new AkkaHttpClient()
    val countryGdpsFuture = for {
      response: HttpResponse <- httpClient.get("https://databank.worldbank.org/data/download/GDP_PPP.csv")
      body: String <- Unmarshal(response.entity).to[String]
    } yield {
      body
        // The file is a little free with it's use of CSV, so I'll just get the lines,
        .split("\n")
        // and parse the cases
        .flatMap {

          // the one with countries with double quotes and yearEarning with double quotes
          case s"""$countryCode,$ranking,,"$countryName","$yearEarning"""" =>
            CountryGdp.apply(countryCode, ranking, countryName, yearEarning)
          // the one with countries with double quotes
          case s"""$countryCode,$ranking,,"$countryName",$yearEarning""" =>
            CountryGdp.apply(countryCode, ranking, countryName, yearEarning)
          // the one with yearEarning with double quotes
          case s"""$countryCode,$ranking,,$countryName,"$yearEarning"""" =>
            CountryGdp.apply(countryCode, ranking, countryName, yearEarning)
          // the one with yearEarning with no double quotes
          case s"""$countryCode,$ranking,,$countryName,$yearEarning""" =>
            CountryGdp.apply(countryCode, ranking, countryName, yearEarning)
          // discard the rest
          case _ => None
        }

    }

    val countryGdps = Await.result(countryGdpsFuture, 20.seconds)

    import entities.marshalling._
    val messages = countryGdps
      .map { toDomain =>
        CountryGrossDomesticProductGlobalRanking(
          toDomain.countryCode,
          toDomain.ranking.toInt
        )
      }
      .map { countryGDP =>
        KafkaKeyValue(
          countryGDP.country,
          serialization.encode(countryGDP)
        )
      }

    messages foreach println

    pub_sub.interpreter.fs2.MessageProducer
      .fs2MessageProducer(MessageBrokerRequirements.productionSettings)(
        ProducedNotification.print(ProducedNotification.producedNotificationStandardPrintFormat)
      )(CountryGrossDomesticProductGlobalRanking.name)(messages.toIndexedSeq)

  }

}
