package application.responses

import domain.GDP
import domain_driven_design.cqrs.{Query, Response}

sealed trait CountryResponses extends Response
object CountryResponses {

  case class GetCountryStateGdpResponse(gdp: GDP) extends CountryResponses

}
