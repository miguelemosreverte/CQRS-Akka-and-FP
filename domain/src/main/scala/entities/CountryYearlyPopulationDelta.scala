package entities

object CountryYearlyPopulationDelta {
  def name = utils.Inference.getSimpleName(this.getClass.getName)
}
case class CountryYearlyPopulationDelta(country: String, year: Int, yearlyPopulationDelta: Long)
