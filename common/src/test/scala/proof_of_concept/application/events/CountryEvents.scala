package proof_of_concept.application.events

import domain_driven_design.cqrs.Event
import proof_of_concept.domain.GDP

sealed trait CountryEvents

object CountryEvents {
  case class AddedGDP(country: String, GDP: GDP) extends Event
}
