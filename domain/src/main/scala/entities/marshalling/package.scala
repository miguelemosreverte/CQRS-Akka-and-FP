package entities

import pub_sub.algebra.KafkaKeyValueLike.KafkaKeyValue
import play.api.libs.json.Json

package object marshalling {

  implicit val CountryGrossDomesticProductGlobalRankingF = Json.format[CountryGrossDomesticProductGlobalRanking]
  implicit def CountryGrossDomesticProductGlobalRankingKafkaValue(
      countryGrossDomesticProductGlobalRanking: CountryGrossDomesticProductGlobalRanking
  ): KafkaKeyValue =
    KafkaKeyValue(
      key = countryGrossDomesticProductGlobalRanking.country,
      value = serialization.encode(countryGrossDomesticProductGlobalRanking)
    )

  implicit val CountryYearlyPopulationDeltaF = Json.format[CountryYearlyPopulationDelta]
  implicit def CountryYearlyPopulationDeltaKafkaValue(
      countryYearlyPopulationDelta: CountryYearlyPopulationDelta
  ): KafkaKeyValue =
    KafkaKeyValue(
      key = countryYearlyPopulationDelta.country,
      value = serialization.encode(countryYearlyPopulationDelta)
    )

  implicit val CountryYearlyTotalPopulationF = Json.format[CountryYearlyTotalPopulation]
  implicit def CountryYearlyTotalPopulationKafkaValue(
      countryYearlyTotalPopulation: CountryYearlyTotalPopulation
  ): KafkaKeyValue =
    KafkaKeyValue(
      key = countryYearlyTotalPopulation.country,
      value = serialization.encode(countryYearlyTotalPopulation)
    )

}
