package application.queries

import domain_driven_design.cqrs.Query

sealed trait CountryQueries
object CountryQueries {
  case class GetCountryStateGDP(country: String) extends Query {
    override def entityId: String = country
  }
}
