package spec

import akka.actor.ActorSystem
import actor_model.ActorSpec
import akka.Done
import application.queries.CountryQueries.GetCountryStateGDP
import pub_sub.algebra.{KafkaKeyValue, MessageProcessor, MessageProducer, PubSub}
import infrastructure.marshalling._
import application.commands.CountryCommands.AddGDP
import application.responses.CountryResponses.GetCountryStateGdpResponse
import infrastructure.consumers.AddGdpTransaction
import pub_sub.algebra.MessageProducer.{MessageProducer, ProducedNotification}
import akka.entity.AskPattern._
import domain.GDP
import infrastructure.CountryActor
import pub_sub.algebra.MessageProcessor.MessageProcessor
import pub_sub.interpreter.alpakka.MessageBrokerRequirements

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
