package country_gdp_ranking.unit_test

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
import pub_sub.algebra.MessageProcessor.MessageProcessor
import pub_sub.algebra.MessageProducer.MessageProducer
import pub_sub.interpreter.KafkaMock

import scala.concurrent.Future

object CountryActorUnitTestSpec {

  def getContext(actorSystem: ActorSystem): CountrySpec.TestContext = {
    // localImplicit @deprecated | in Scala 3 we will be able to send first order functions with implicit parameters
    implicit val s: ActorSystem = actorSystem

    val kafkaMock = new KafkaMock

    val messageProcessor: MessageProcessor[Unit, Future[Done]] =
      topic => consumerGroup => { algorithm => kafkaMock.run(AddGdpTransaction.topic, algorithm) }
    val messageProducer: MessageProducer[Future[Done]] =
      handler => topic => data => kafkaMock.produce(data, topic)(handler)

    val actor = CountryActor.start(messageProducer)

    kafkaMock run (
      topic = AddGdpTransaction.topic,
      algorithm = AddGdpTransaction.processMessage(actor)
    )

    CountrySpec.TestContext(
      messageProcessor = messageProcessor,
      messageProducer = messageProducer
    )
  }

}

class CountryActorUnitTestSpec
    extends CountrySpec(
      CountryActorUnitTestSpec getContext
    )
