package country_gdp_ranking.application.responses

import country_gdp_ranking.domain.GDP
import domain_driven_design.cqrs.{Query, Response}

sealed trait CountryResponses extends Response
object CountryResponses {

  case class GetCountryStateGdpResponse(gdp: GDP) extends CountryResponses

}
