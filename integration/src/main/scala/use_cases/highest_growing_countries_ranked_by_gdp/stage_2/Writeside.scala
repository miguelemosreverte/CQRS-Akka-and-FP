package use_cases.highest_growing_countries_ranked_by_gdp.stage_2

import akka.Done
import akka.actor.ActorSystem
import akka.stream.UniqueKillSwitch
import utils.Generators

import scala.concurrent.{ExecutionContextExecutor, Future}
import pub_sub.interpreter.alpakka.MessageBrokerRequirements
import country_yearly_population_delta.infrastructure.consumers.AddYearlyPopulationGrowthTransaction
import pub_sub.algebra.MessageProducer.MessageProducer
import country_yearly_population_delta.infrastructure.consumers.AddYearlyPopulationGrowthTransaction
object Writeside extends App {

  val availablePort: Int = 2551
  val actorSystem = Generators.actorSystem(availablePort, "ActorSystemParallelizerBuilder")
  implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher
  val messageBrokerRequirements =
    MessageBrokerRequirements.productionSettings(AddYearlyPopulationGrowthTransaction.topic, "default")(actorSystem, ec)
  val messageProducer = pub_sub.interpreter.alpakka.MessageProducer.alpakkaMessageProducer(messageBrokerRequirements)
  startConsumers(messageProducer)(actorSystem)

  def startConsumers(
      messageProducer: MessageProducer[Future[Done]]
  )(implicit system: ActorSystem): Seq[UniqueKillSwitch] = {
    println("START CountryYearlyTotalPopulation consumption")
    val populationActor = country_yearly_population_delta.infrastructure.CountryActor.start(messageProducer)

    Seq(
      pub_sub.interpreter.alpakka.PubSub.PubSubAlpakka
        .messageProcessor(messageBrokerRequirements)("default")(
          AddYearlyPopulationGrowthTransaction.topic
        )(AddYearlyPopulationGrowthTransaction processMessage populationActor)
    )

  }
}
