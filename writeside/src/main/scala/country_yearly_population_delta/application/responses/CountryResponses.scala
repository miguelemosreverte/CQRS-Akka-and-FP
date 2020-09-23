package country_yearly_population_delta.application.responses
import domain_driven_design.cqrs.Response

sealed trait CountryResponses extends Response
object CountryResponses {

  case class GetCountryAveragePopulationGrowthResponse(yearlyAveragePopulationGrowth: Long) extends CountryResponses

}
