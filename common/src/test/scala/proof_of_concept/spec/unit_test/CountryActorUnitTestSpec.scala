package proof_of_concept.spec.unit_test

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
import pub_sub.interpreter.KafkaMock

import scala.concurrent.Future

object CountryActorUnitTestSpec {

  def getContext(actorSystem: ActorSystem): CountrySpec.TestContext = {
    // localImplicit @deprecated | in Scala 3 we will be able to send first order functions with implicit parameters
    implicit val s: ActorSystem = actorSystem

    val actor = CountryActor.start
    val kafkaMock = new KafkaMock

    kafkaMock run (
      algorithm = AddGdpTransaction.processMessage(actor)
    )

    CountrySpec.TestContext(
      messageProcessor = kafkaMock,
      messageProducer = kafkaMock
    )
  }

}

class CountryActorUnitTestSpec
    extends CountrySpec(
      CountryActorUnitTestSpec getContext
    )
