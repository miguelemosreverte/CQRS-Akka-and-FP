package proof_of_concept.spec.unit_test

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
import pub_sub.algebra.MessageProcessor.MessageProcessor
import pub_sub.algebra.MessageProducer.MessageProducer
import pub_sub.algebra.{KafkaKeyValue, MessageProcessor, PubSub}
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

    val a: MessageProcessor[Unit, Future[Done]] =
      topic => consumerGroup => { kafkaMock.run _ }
    val b: MessageProducer[Future[Done]] = data =>
      topic =>
        handler => {
          kafkaMock.produce(data, topic)(handler)
        }
    CountrySpec.TestContext(
      messageProcessor = a,
      messageProducer = b
    )
  }

}

class CountryActorUnitTestSpec
    extends CountrySpec(
      CountryActorUnitTestSpec getContext
    )
