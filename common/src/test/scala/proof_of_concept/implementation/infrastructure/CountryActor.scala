package proof_of_concept.implementation.infrastructure

import actor_model.BasePersistentActor
import akka.actor.Props
import akka.entity.ShardedEntity
import proof_of_concept.implementation.application.CountryState
import proof_of_concept.implementation.application.commands.CountryCommands
import proof_of_concept.implementation.application.events.CountryEvents
import proof_of_concept.implementation.domain.GDP

class CountryActor extends BasePersistentActor[CountryEvents, CountryState, CountryState] {
  override var state: CountryState = CountryState(GDP = GDP(0))

  override def receiveCommand: Receive = {
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
