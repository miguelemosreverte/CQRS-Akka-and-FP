package country_yearly_population_delta.application.events

import domain_driven_design.cqrs.Event

sealed trait CountryEvents extends Event

object CountryEvents {
  case class AddedYearlyCountryPopulation(country: String, year: Int, totalPopulation: Long) extends CountryEvents
  case class AddedYearlyCountryPopulationGrowth(country: String, year: Int, yearlyPopulationGrowth: Long)
  //extends CountryEvents
}
