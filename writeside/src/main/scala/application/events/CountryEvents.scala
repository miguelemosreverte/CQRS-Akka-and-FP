package application.events

import domain.GDP
import domain_driven_design.cqrs.Event

sealed trait CountryEvents extends Event

object CountryEvents {
  case class AddedGDP(country: String, GDP: GDP) extends CountryEvents
}
