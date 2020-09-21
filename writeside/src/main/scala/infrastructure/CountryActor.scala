package infrastructure

import actor_model.BasePersistentActor
import akka.actor.Props
import akka.entity.ShardedEntity
import application.CountryState
import application.commands.CountryCommands
import application.events.CountryEvents
import application.queries.CountryQueries
import application.responses.CountryResponses
import domain.GDP

class CountryActor extends BasePersistentActor[CountryEvents, CountryState] {
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

object CountryActor extends ShardedEntity {
  override def props: Props = Props(new CountryActor())
}
