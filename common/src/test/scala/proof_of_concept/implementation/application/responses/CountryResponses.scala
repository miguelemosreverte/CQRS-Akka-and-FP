package proof_of_concept.implementation.application.responses

import domain_driven_design.cqrs.{Query, Response}
import proof_of_concept.implementation.domain.GDP

sealed trait CountryResponses extends Response
object CountryResponses {

  case class GetCountryStateGdpResponse(gdp: GDP) extends CountryResponses

}
