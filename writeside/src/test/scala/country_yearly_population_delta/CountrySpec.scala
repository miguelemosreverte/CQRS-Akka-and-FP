package country_yearly_population_delta

import actor_model.ActorSpec
import akka.actor.ActorSystem
import akka.entity.AskPattern._
import pub_sub.algebra.KafkaKeyValueLike.KafkaKeyValue
import pub_sub.algebra.MessageProcessor.MessageProcessor
import country_yearly_population_delta.infrastructure.CountryActor
import country_yearly_population_delta.infrastructure.marshalling._
import pub_sub.algebra.MessageProducer.{MessageProducer, ProducedNotification}
import country_yearly_population_delta.infrastructure.consumers.AddYearlyPopulationGrowthTransaction
import country_yearly_population_delta.application.commands.CountryCommands.AddYearlyCountryPopulation
import country_yearly_population_delta.application.queries.CountryQueries.GetCountryAveragePopulationGrowth
import country_yearly_population_delta.application.responses.CountryResponses.GetCountryAveragePopulationGrowthResponse
import pub_sub.interpreter.alpakka.MessageBrokerRequirements

object CountrySpec {
  case class TestContext(messageProducer: MessageProducer[_], messageProcessor: MessageProcessor[_, _])
}
abstract class CountrySpec(
    getContext: ActorSystem => CountrySpec.TestContext
) extends ActorSpec {

  "sending AddYearlyCountryPopulation to AddYearlyPopulationGrowthTransaction" should
  "modify the CoutryActor state" in parallelActorSystemRunner { implicit s =>
    val context = getContext(s)
    val actor = CountryActor.start(context.messageProducer)
    val examples = Seq(
      AddYearlyCountryPopulation("Argentina", 2000, 1000),
      AddYearlyCountryPopulation("Argentina", 2001, 2000),
      AddYearlyCountryPopulation("Argentina", 2002, 3000)
    )
    context.messageProducer(ProducedNotification.print(ProducedNotification.producedNotificationStandardPrintFormat))(
      AddYearlyPopulationGrowthTransaction.topic
    )(examples map { example =>
      KafkaKeyValue(
        key = example.country,
        value = serialization.encode(example)
      )
    })

    eventually {
      val response: GetCountryAveragePopulationGrowthResponse =
        actor
          .ask[GetCountryAveragePopulationGrowthResponse](
            GetCountryAveragePopulationGrowth("Argentina")
          )
          .futureValue

      println(s"response ${response} should be ${1000}")

      response.yearlyAveragePopulationGrowth should be(1000)
    }

  }

}
