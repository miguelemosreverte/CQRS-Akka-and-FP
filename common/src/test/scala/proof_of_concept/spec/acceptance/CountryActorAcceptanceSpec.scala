package proof_of_concept.spec.acceptance

import akka.Done
import akka.actor.ActorSystem
import org.scalatest.flatspec.AnyFlatSpec
import proof_of_concept.implementation.application.commands.CountryCommands
import proof_of_concept.implementation.domain.GDP
import proof_of_concept.implementation.infrastructure.CountryActor
import akka.entity.AskPattern._
import akka.stream.UniqueKillSwitch
import org.scalatest.matchers.must.Matchers.{be, convertToAnyMustWrapper}
import org.slf4j.{Logger, LoggerFactory}
import proof_of_concept.implementation.application.queries.CountryQueries
import proof_of_concept.implementation.infrastructure.consumers.AddGdpTransaction
import proof_of_concept.spec.CountrySpec
import pub_sub.algebra.MessageProcessor
import pub_sub.algebra.MessageProcessor.MessageProcessor
import pub_sub.interpreter.utils.KafkaMessageBrokerRequirements

import scala.concurrent.{ExecutionContextExecutor, Future}

object CountryActorAcceptanceSpec {

  def getContext(actorSystem: ActorSystem): CountrySpec.TestContext = {
    // localImplicit @deprecated | in Scala 3 we will be able to send first order functions with implicit parameters
    implicit val s: ActorSystem = actorSystem
    implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher

    val r = KafkaMessageBrokerRequirements.productionSettings(AddGdpTransaction.topic, "default")

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
