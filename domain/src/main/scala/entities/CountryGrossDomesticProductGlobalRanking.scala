package entities

object CountryGrossDomesticProductGlobalRanking {
  def name = utils.Inference.getSimpleName(this.getClass.getName)
}
case class CountryGrossDomesticProductGlobalRanking(country: String, globalRanking: Int)
