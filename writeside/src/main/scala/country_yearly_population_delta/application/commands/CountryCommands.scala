package country_yearly_population_delta.application.commands

import country_gdp_ranking.domain.GDP
import domain_driven_design.cqrs.Command

trait CountryCommands

object CountryCommands {
  case class AddYearlyCountryPopulation(country: String, year: Int, population: Long) extends Command {
    override def entityId: String = country
  }
}
