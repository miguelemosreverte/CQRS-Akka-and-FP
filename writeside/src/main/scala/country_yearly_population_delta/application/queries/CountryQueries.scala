package country_yearly_population_delta.application.queries

import domain_driven_design.cqrs.Query

sealed trait CountryQueries
object CountryQueries {
  case class GetCountryAveragePopulationGrowth(country: String) extends Query {
    override def entityId: String = country
  }
}
