package proof_of_concept.spec.acceptance

import akka.Done
import akka.actor.ActorSystem
import org.scalatest.flatspec.AnyFlatSpec
import proof_of_concept.implementation.application.commands.CountryCommands
import proof_of_concept.implementation.domain.GDP
import proof_of_concept.implementation.infrastructure.CountryActor
import akka.entity.AskPattern._
import consumers_spec.no_registrales.obligacion.CountrySpec
import org.scalatest.matchers.must.Matchers.{be, convertToAnyMustWrapper}
import org.slf4j.{Logger, LoggerFactory}
import proof_of_concept.implementation.application.queries.CountryQueries
import proof_of_concept.implementation.infrastructure.consumers.AddGdpTransaction
import pub_sub.algebra.MessageProcessor
import pub_sub.interpreter.utils.KafkaMessageProcessorRequirements
import pub_sub.interpreter.{KafkaMessageProducer, KafkaMock, KafkaTransactionalMessageProcessor}

import scala.concurrent.{ExecutionContextExecutor, Future}

object CountryActorAcceptanceSpec {

  def getContext(actorSystem: ActorSystem): CountrySpec.TestContext = {
    // localImplicit @deprecated | in Scala 3 we will be able to send first order functions with implicit parameters
    implicit val s: ActorSystem = actorSystem
    implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher
    val messageProcessor = KafkaTransactionalMessageProcessor.KafkaTransactionalMessageProcessor
      .productionSettings(AddGdpTransaction.topic)
    val messageProducer = KafkaMessageProducer.productionSettings

    messageProcessor run (AddGdpTransaction processMessage (CountryActor start))

    CountrySpec.TestContext(
      messageProcessor = messageProcessor,
      messageProducer = messageProducer
    )
  }

}
class CountryActorAcceptanceSpec
    extends CountrySpec(
      CountryActorAcceptanceSpec getContext
    )
