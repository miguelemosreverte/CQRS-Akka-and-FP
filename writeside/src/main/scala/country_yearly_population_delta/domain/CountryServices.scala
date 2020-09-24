package country_yearly_population_delta.domain

import country_yearly_population_delta.application.events.CountryEvents.AddedYearlyCountryPopulation

object CountryServices {
  def populationGrowthByYear(addedYearlyCountryPopulation: AddedYearlyCountryPopulation,
                             countryState: CountryState
  ): Map[Int, Long] = {
    val year: Int = addedYearlyCountryPopulation.year
    val yearPopulation: Long = addedYearlyCountryPopulation.totalPopulation
    countryState.populationByYear.get(year + 1) match {
      case Some(nextYearPopulation: Long) =>
        val populationDelta = nextYearPopulation - yearPopulation
        countryState.populationGrowthByYear + ((year -> populationDelta))
      case None =>
        countryState.populationGrowthByYear
    }
  }

  def averagePopulationGrowth(countryState: CountryState): Long = {
    def efficientAverage: Iterable[Long] => Double =
      _.foldLeft((0.0, 1)) { (acc, i) =>
        ((acc._1 + (i - acc._1) / acc._2), acc._2 + 1)
      }._1
    efficientAverage(countryState.populationGrowthByYear.values).toLong
  }

}
