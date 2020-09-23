package country_gdp_ranking.acceptance

import akka.Done
import akka.actor.ActorSystem
import org.scalatest.flatspec.AnyFlatSpec
import country_gdp_ranking.application.commands.CountryCommands
import akka.entity.AskPattern._
import akka.stream.UniqueKillSwitch
import country_gdp_ranking.domain.GDP
import org.scalatest.matchers.must.Matchers.{be, convertToAnyMustWrapper}
import org.slf4j.{Logger, LoggerFactory}
import country_gdp_ranking.application.queries.CountryQueries
import country_gdp_ranking.infrastructure.CountryActor
import country_gdp_ranking.infrastructure.consumers.AddGdpTransaction
import country_gdp_ranking.CountrySpec
import org.scalatest.Ignore
import pub_sub.algebra.MessageProcessor
import pub_sub.algebra.MessageProcessor.MessageProcessor
import pub_sub.interpreter.alpakka.MessageBrokerRequirements

import scala.concurrent.{ExecutionContextExecutor, Future}

object CountryActorAcceptanceSpec {

  def getContext(actorSystem: ActorSystem): CountrySpec.TestContext = {
    // localImplicit @deprecated | in Scala 3 we will be able to send first order functions with implicit parameters
    implicit val s: ActorSystem = actorSystem
    implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher

    val r = MessageBrokerRequirements.productionSettings(AddGdpTransaction.topic, "default")

    val messageProducer = pub_sub.interpreter.alpakka.MessageProducer.alpakkaMessageProducer(r)
    val actor = CountryActor.start(messageProducer)
    val started: UniqueKillSwitch = pub_sub.interpreter.alpakka.PubSub.PubSubAlpakka
      .messageProcessor(r)("default")(AddGdpTransaction.topic)(AddGdpTransaction processMessage actor)

    CountrySpec.TestContext(
      messageProcessor = pub_sub.interpreter.alpakka.PubSub.PubSubAlpakka.messageProcessor(r),
      messageProducer = pub_sub.interpreter.alpakka.PubSub.PubSubAlpakka.messageProducer(r)
    )
  }

}

@Ignore
class CountryActorAcceptanceSpec
    extends CountrySpec(
      CountryActorAcceptanceSpec getContext
    )
