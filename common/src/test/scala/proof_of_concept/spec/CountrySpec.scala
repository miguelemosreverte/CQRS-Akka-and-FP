package proof_of_concept.spec

import akka.actor.ActorSystem
import actor_model.ActorSpec
import akka.Done
import proof_of_concept.implementation.application.queries.CountryQueries.GetCountryStateGDP
import pub_sub.algebra.{KafkaKeyValue, MessageProcessor, MessageProducer, PubSub}
import proof_of_concept.implementation.infrastructure.marshalling._
import proof_of_concept.implementation.application.commands.CountryCommands.AddGDP
import proof_of_concept.implementation.application.responses.CountryResponses.GetCountryStateGdpResponse
import proof_of_concept.implementation.infrastructure.CountryActor
import proof_of_concept.implementation.infrastructure.consumers.AddGdpTransaction
import proof_of_concept.implementation.domain.GDP
import pub_sub.algebra.MessageProducer.{MessageProducer, ProducedNotification}
import akka.entity.AskPattern._
import pub_sub.algebra.MessageProcessor.MessageProcessor
import pub_sub.interpreter.utils.KafkaMessageBrokerRequirements

import scala.concurrent.Future

object CountrySpec {
  case class TestContext(messageProducer: MessageProducer[_], messageProcessor: MessageProcessor[_, _])
}
abstract class CountrySpec(
    getContext: ActorSystem => CountrySpec.TestContext
) extends ActorSpec {

  "sending AddGDP to AddGdpTransaction" should
  "modify the CoutryActor state" in parallelActorSystemRunner { implicit s =>
    val actor = CountryActor.start
    val example = AddGDP("Argentina", GDP(10))
    getContext(s).messageProducer(
      Seq(
        KafkaKeyValue(
          key = example.country,
          value = serialization.encode(example)
        )
      )
    )(
      AddGdpTransaction.topic
    )(ProducedNotification.print(ProducedNotification.producedNotificationStandardPrintFormat))

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
