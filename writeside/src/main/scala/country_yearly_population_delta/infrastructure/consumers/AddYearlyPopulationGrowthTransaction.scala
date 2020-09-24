package country_yearly_population_delta.infrastructure.consumers

import akka.Done
import akka.actor.ActorRef
import country_yearly_population_delta.application.commands.CountryCommands.AddYearlyCountryPopulation

import scala.concurrent.Future
import country_yearly_population_delta.infrastructure.marshalling._
import akka.entity.AskPattern._
import entities.CountryYearlyTotalPopulation
import pub_sub.algebra.KafkaKeyValueLike.KafkaKeyValue
import entities.marshalling._

object AddYearlyPopulationGrowthTransaction {
  val topic = CountryYearlyTotalPopulation.name

  def processMessage(actorRef: ActorRef)(input: KafkaKeyValue): Either[Throwable, Future[Done]] = {
    println("CountryYearlyTotalPopulation received input")
    serialization
      .decode[CountryYearlyTotalPopulation](input.json)
      .map { serialized =>
        actorRef.ask[akka.Done](
          AddYearlyCountryPopulation(
            serialized.country,
            serialized.year,
            serialized.totalPopulation
          )
        )
      }
  }

}
