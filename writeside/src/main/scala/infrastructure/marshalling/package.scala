package infrastructure

import domain.GDP
import play.api.libs.json.{Format, Json}
import application.commands.CountryCommands
import application.events.CountryEvents
import application.queries.CountryQueries
import application.responses.CountryResponses
import serialization.EventSerializer

package object marshalling {
  implicit val GdpF: Format[GDP] =
    Json.format[GDP]

  implicit val GetCountryStateGdpF: Format[CountryQueries.GetCountryStateGDP] =
    Json.format[CountryQueries.GetCountryStateGDP]
  implicit val GetCountryStateGdpResponseF: Format[CountryResponses.GetCountryStateGdpResponse] =
    Json.format[CountryResponses.GetCountryStateGdpResponse]

  implicit val AddGdpF: Format[CountryCommands.AddGDP] = Json.format[CountryCommands.AddGDP]
  implicit val AddedGdpF: Format[CountryEvents.AddedGDP] = Json.format[CountryEvents.AddedGDP]

  class AddedGDPFS extends EventSerializer[CountryEvents.AddedGDP]

}
