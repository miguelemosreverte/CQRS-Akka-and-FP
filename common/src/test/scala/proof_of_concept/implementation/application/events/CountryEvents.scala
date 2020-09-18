package proof_of_concept.implementation.application.events

import domain_driven_design.cqrs.Event
import proof_of_concept.implementation.domain.GDP

sealed trait CountryEvents extends Event

object CountryEvents {
  case class AddedGDP(country: String, GDP: GDP) extends CountryEvents
}
