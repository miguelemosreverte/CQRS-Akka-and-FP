package spec.acceptance

import akka.Done
import akka.actor.ActorSystem
import org.scalatest.flatspec.AnyFlatSpec
import application.commands.CountryCommands
import akka.entity.AskPattern._
import akka.stream.UniqueKillSwitch
import domain.GDP
import org.scalatest.matchers.must.Matchers.{be, convertToAnyMustWrapper}
import org.slf4j.{Logger, LoggerFactory}
import application.queries.CountryQueries
import infrastructure.CountryActor
import infrastructure.consumers.AddGdpTransaction
import spec.CountrySpec
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

    val started: UniqueKillSwitch = pub_sub.interpreter.alpakka.PubSub.PubSubAlpakka
      .messageProcessor(r)("default")(AddGdpTransaction.topic)(AddGdpTransaction processMessage (CountryActor start))

    CountrySpec.TestContext(
      messageProcessor = pub_sub.interpreter.alpakka.PubSub.PubSubAlpakka.messageProcessor(r),
      messageProducer = pub_sub.interpreter.alpakka.PubSub.PubSubAlpakka.messageProducer(r)
    )
  }

}
class CountryActorAcceptanceSpec
    extends CountrySpec(
      CountryActorAcceptanceSpec getContext
    )
