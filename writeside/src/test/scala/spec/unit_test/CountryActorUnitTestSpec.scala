package spec.unit_test

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
      topic = AddGdpTransaction.topic,
      algorithm = AddGdpTransaction.processMessage(actor)
    )

    val a: MessageProcessor[Unit, Future[Done]] =
      topic => consumerGroup => { algorithm => kafkaMock.run(AddGdpTransaction.topic, algorithm) }
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
