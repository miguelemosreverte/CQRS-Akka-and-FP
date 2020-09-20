package proof_of_concept.implementation.infrastructure

import play.api.libs.json.{Format, Json}
import proof_of_concept.implementation.application.commands.CountryCommands
import proof_of_concept.implementation.application.events.CountryEvents
import proof_of_concept.implementation.application.queries.CountryQueries
import proof_of_concept.implementation.application.responses.CountryResponses
import proof_of_concept.implementation.domain.GDP
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
