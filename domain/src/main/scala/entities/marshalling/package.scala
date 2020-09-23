package entities

import pub_sub.algebra.KafkaKeyValueLike.KafkaKeyValue
import play.api.libs.json.Json

package object marshalling {

  implicit val CountryGrossDomesticProductGlobalRankingF = Json.format[CountryGrossDomesticProductGlobalRanking]
  implicit val CountryYearlyTotalPopulationF = Json.format[CountryYearlyTotalPopulation]

  implicit def CountryGrossDomesticProductGlobalRankingKafkaValue(
      countryGrossDomesticProductGlobalRanking: CountryGrossDomesticProductGlobalRanking
  ): KafkaKeyValue =
    KafkaKeyValue(
      key = countryGrossDomesticProductGlobalRanking.country,
      value = serialization.encode(countryGrossDomesticProductGlobalRanking)
    )
}
