package country_gdp_ranking.infrastructure

import country_gdp_ranking.domain.GDP
import play.api.libs.json.{Format, Json}
import country_gdp_ranking.application.commands.CountryCommands
import country_gdp_ranking.application.events.CountryEvents
import country_gdp_ranking.application.queries.CountryQueries
import country_gdp_ranking.application.responses.CountryResponses
import serialization.EventSerializer

package object marshalling {
  implicit val GdpF: Format[GDP] =
    Json.format[GDP]
  implicit val GetCountryStateGdpF =
    Json.format[CountryQueries.GetCountryStateGDP]
  implicit val GetCountryStateGdpResponseF =
    Json.format[CountryResponses.GetCountryStateGdpResponse]
  implicit val AddGdpF =
    Json.format[CountryCommands.AddGDP]
  implicit val AddedGdpF =
    Json.format[CountryEvents.AddedGDP]

  class AddedGDPFS extends EventSerializer[CountryEvents.AddedGDP]

}
