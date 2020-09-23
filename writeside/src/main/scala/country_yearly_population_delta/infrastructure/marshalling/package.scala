package country_yearly_population_delta.infrastructure

import play.api.libs.json.{Format, Json}
import country_yearly_population_delta.application.commands.CountryCommands
import country_yearly_population_delta.application.events.CountryEvents
import country_yearly_population_delta.application.queries.CountryQueries
import country_yearly_population_delta.application.responses.CountryResponses
import serialization.EventSerializer

package object marshalling {

  implicit val GetCountryAveragePopulationGrowthF =
    Json.format[CountryQueries.GetCountryAveragePopulationGrowth]
  implicit val GetCountryAveragePopulationGrowthResponseF =
    Json.format[CountryResponses.GetCountryAveragePopulationGrowthResponse]
  implicit val AddYearlyCountryPopulationF =
    Json.format[CountryCommands.AddYearlyCountryPopulation]
  implicit val AddedYearlyCountryPopulationF =
    Json.format[CountryEvents.AddedYearlyCountryPopulation]

  class AddedYearlyCountryPopulationFS extends EventSerializer[CountryEvents.AddedYearlyCountryPopulation]

}
