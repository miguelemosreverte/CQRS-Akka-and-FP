package country_yearly_population_delta.acceptance

import org.scalatest.Ignore
import akka.actor.ActorSystem
import akka.stream.UniqueKillSwitch
import country_gdp_ranking.CountrySpec
import scala.concurrent.ExecutionContextExecutor
import country_gdp_ranking.infrastructure.CountryActor
import pub_sub.interpreter.alpakka.MessageBrokerRequirements
import country_yearly_population_delta.infrastructure.consumers.AddYearlyPopulationGrowthTransaction

object CountryActorAcceptanceSpec {

  def getContext(actorSystem: ActorSystem): CountrySpec.TestContext = {
    // localImplicit @deprecated | in Scala 3 we will be able to send first order functions with implicit parameters
    implicit val s: ActorSystem = actorSystem
    implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher

    val r = MessageBrokerRequirements.productionSettings(AddYearlyPopulationGrowthTransaction.topic, "default")
    val messageProducer = pub_sub.interpreter.alpakka.MessageProducer.alpakkaMessageProducer(r)
    val actor = CountryActor.start(messageProducer)
    val started: UniqueKillSwitch = pub_sub.interpreter.alpakka.PubSub.PubSubAlpakka
      .messageProcessor(r)("default")(AddYearlyPopulationGrowthTransaction.topic)(
        AddYearlyPopulationGrowthTransaction processMessage actor
      )

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
