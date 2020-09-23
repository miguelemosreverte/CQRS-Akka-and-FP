package country_gdp_ranking.application.events

import country_gdp_ranking.domain.GDP
import domain_driven_design.cqrs.Event

sealed trait CountryEvents extends Event

object CountryEvents {
  case class AddedGDP(country: String, GDP: GDP) extends CountryEvents
}
