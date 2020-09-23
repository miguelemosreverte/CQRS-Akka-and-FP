package country_gdp_ranking

import actor_model.ActorSpec
import akka.actor.ActorSystem
import akka.entity.AskPattern._
import pub_sub.algebra.KafkaKeyValueLike.KafkaKeyValue
import country_gdp_ranking.domain.GDP
import country_gdp_ranking.infrastructure.CountryActor
import country_gdp_ranking.infrastructure.marshalling._
import pub_sub.algebra.MessageProcessor.MessageProcessor
import country_gdp_ranking.infrastructure.consumers.AddGdpTransaction
import country_gdp_ranking.application.commands.CountryCommands.AddGDP
import pub_sub.algebra.MessageProducer.{MessageProducer, ProducedNotification}
import country_gdp_ranking.application.queries.CountryQueries.GetCountryStateGDP
import country_gdp_ranking.application.responses.CountryResponses.GetCountryStateGdpResponse

object CountrySpec {
  case class TestContext(messageProducer: MessageProducer[_], messageProcessor: MessageProcessor[_, _])
}
abstract class CountrySpec(
    getContext: ActorSystem => CountrySpec.TestContext
) extends ActorSpec {

  "sending AddGDP to AddGdpTransaction" should
  "modify the CoutryActor state" in parallelActorSystemRunner { implicit s =>
    val context = getContext(s)
    val actor = CountryActor.start(context.messageProducer)
    val example = AddGDP("Argentina", GDP(10))
    context.messageProducer(ProducedNotification.print(ProducedNotification.producedNotificationStandardPrintFormat))(
      AddGdpTransaction.topic
    )(
      Seq(
        KafkaKeyValue(
          key = example.country,
          value = serialization.encode(example)
        )
      )
    )

    eventually {
      val response: GetCountryStateGdpResponse =
        actor
          .ask[GetCountryStateGdpResponse](
            GetCountryStateGDP("Argentina")
          )
          .futureValue

      println(s"response.gdp ${response.gdp} should be ${GDP(10)}")

      response.gdp should be(GDP(10))
    }

  }

}
