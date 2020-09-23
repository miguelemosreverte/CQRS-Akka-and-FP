package entities

object CountryYearlyTotalPopulation {
  def name = utils.Inference.getSimpleName(this.getClass.getName)
}
case class CountryYearlyTotalPopulation(country: String, year: Int, totalPopulation: Long)
