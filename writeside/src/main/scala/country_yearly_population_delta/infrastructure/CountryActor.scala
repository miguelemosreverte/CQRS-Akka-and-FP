package country_yearly_population_delta.infrastructure

import actor_model.BasePersistentActor
import akka.actor.Props
import akka.entity.ShardedEntity
import country_yearly_population_delta.application.commands.CountryCommands
import country_yearly_population_delta.application.events.CountryEvents
import country_yearly_population_delta.application.queries.CountryQueries
import country_yearly_population_delta.application.responses.CountryResponses
import country_yearly_population_delta.domain.CountryState
import pub_sub.algebra.KafkaKeyValueLike.KafkaKeyValue
import pub_sub.algebra.MessageProducer
import pub_sub.algebra.MessageProducer.MessageProducer
import pub_sub.interpreter.alpakka.MessageBrokerRequirements

class CountryActor(messageProducer: MessageProducer[_]) extends BasePersistentActor[CountryEvents, CountryState] {
  override var state: CountryState = CountryState()

  def AveragePopulationGrowthResponse: Long =
    0
  override def receiveCommand: Receive = {
    case CountryQueries.GetCountryAveragePopulationGrowth(country) =>
      sender() ! CountryResponses.GetCountryAveragePopulationGrowthResponse(state.averagePopulationGrowth)
    case CountryCommands.AddYearlyCountryPopulation(country, year, population) =>
      val event = CountryEvents.AddedYearlyCountryPopulation(country, year, population)
      persist(event) { _ =>
        state += event
        sender() ! akka.Done

        state.populationGrowthByYear.get(year).map { populationGrowth: Long =>
          messageProducer(MessageProducer.ProducedNotification.Defaults.defaultPrint)("topic")(
            Seq(
              KafkaKeyValue("", "")
            )
          )
        }

      }
  }
}

object CountryActor extends ShardedEntity[MessageProducer[_]] {
  override def props(messageBrokerRequirements: MessageProducer[_]): Props =
    Props(new CountryActor(messageBrokerRequirements))
}
