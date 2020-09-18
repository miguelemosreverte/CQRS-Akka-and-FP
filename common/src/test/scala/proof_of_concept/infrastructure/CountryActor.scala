package proof_of_concept.infrastructure

import proof_of_concept.application.CountryState
import proof_of_concept.application.commands.CountryCommands
import proof_of_concept.application.events.CountryEvents
import proof_of_concept.domain.GDP

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
