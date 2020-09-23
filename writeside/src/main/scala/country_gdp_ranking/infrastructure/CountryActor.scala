package country_gdp_ranking.infrastructure

import akka.actor.Props
import akka.entity.ShardedEntity
import actor_model.BasePersistentActor
import country_gdp_ranking.domain.{CountryState, GDP}
import country_gdp_ranking.application.events.CountryEvents
import country_gdp_ranking.application.queries.CountryQueries
import country_gdp_ranking.application.commands.CountryCommands
import country_gdp_ranking.application.responses.CountryResponses
import pub_sub.algebra.MessageProducer.MessageProducer

class CountryActor(messageProducer: MessageProducer[_]) extends BasePersistentActor[CountryEvents, CountryState] {
  override var state: CountryState = CountryState(GDP = GDP(0))

  override def receiveCommand: Receive = {
    case CountryQueries.GetCountryStateGDP(country) =>
      sender() ! CountryResponses.GetCountryStateGdpResponse(state.GDP)
    case CountryCommands.AddGDP(country, gdp) =>
      val event = CountryEvents.AddedGDP(country, gdp)
      persist(event) { _ =>
        state += event
        sender() ! akka.Done
      }
  }
}

object CountryActor extends ShardedEntity[MessageProducer[_]] {
  override def props(messageBrokerRequirements: MessageProducer[_]): Props =
    Props(new CountryActor(messageBrokerRequirements))
}
