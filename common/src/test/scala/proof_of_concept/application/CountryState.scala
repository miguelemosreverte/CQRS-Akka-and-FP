package proof_of_concept.application

import domain_driven_design.cqrs.State
import proof_of_concept.application.events.CountryEvents
import proof_of_concept.application.events.CountryEvents.AddedGDP
import proof_of_concept.domain.GDP

case class CountryState(GDP: GDP) extends State[CountryEvents, CountryState] {
  override def +(event: CountryEvents): CountryState =
    event match {
      case AddedGDP(_, GDP) =>
        copy(
          GDP = GDP
        )
    }
}
