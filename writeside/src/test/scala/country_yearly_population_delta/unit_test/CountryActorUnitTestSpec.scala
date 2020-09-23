package country_yearly_population_delta.unit_test

import akka.Done
import akka.actor.ActorSystem
import pub_sub.interpreter.KafkaMock
import country_yearly_population_delta.CountrySpec
import pub_sub.algebra.MessageProducer.MessageProducer
import pub_sub.algebra.MessageProcessor.MessageProcessor
import country_yearly_population_delta.infrastructure.CountryActor
import country_yearly_population_delta.infrastructure.consumers.AddYearlyPopulationGrowthTransaction

import scala.concurrent.Future

object CountryActorUnitTestSpec {

  def getContext(actorSystem: ActorSystem): CountrySpec.TestContext = {
    // localImplicit @deprecated | in Scala 3 we will be able to send first order functions with implicit parameters
    implicit val s: ActorSystem = actorSystem

    val kafkaMock = new KafkaMock

    val messageProcessor: MessageProcessor[Unit, Future[Done]] =
      topic => consumerGroup => { algorithm => kafkaMock.run(AddYearlyPopulationGrowthTransaction.topic, algorithm) }

    val messageProducer: MessageProducer[Future[Done]] =
      handler => topic => data => kafkaMock.produce(data, topic)(handler)

    val actor = CountryActor.start(messageProducer)

    kafkaMock run (
      topic = AddYearlyPopulationGrowthTransaction.topic,
      algorithm = AddYearlyPopulationGrowthTransaction.processMessage(actor)
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
