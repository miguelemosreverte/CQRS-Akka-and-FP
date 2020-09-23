package country_yearly_population_delta.infrastructure.consumers

import akka.Done
import akka.actor.ActorRef
import country_yearly_population_delta.application.commands.CountryCommands.AddYearlyCountryPopulation

import scala.concurrent.Future
import country_yearly_population_delta.infrastructure.marshalling._
import akka.entity.AskPattern._
import pub_sub.algebra.KafkaKeyValueLike.KafkaKeyValue
object AddYearlyPopulationGrowthTransaction {
  val topic = "AddYearlyPopulationGrowthTransaction"

  def processMessage(actorRef: ActorRef)(input: KafkaKeyValue): Either[Throwable, Future[Done]] = {
    serialization
      .decode[AddYearlyCountryPopulation](input.json)
      .map { serialized =>
        actorRef.ask[akka.Done](serialized)
      }
  }

}
