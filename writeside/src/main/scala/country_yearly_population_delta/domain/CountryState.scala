package country_yearly_population_delta.domain

import country_yearly_population_delta.application.events.CountryEvents
import country_yearly_population_delta.application.events.CountryEvents.AddedYearlyCountryPopulation
import domain_driven_design.cqrs.State

case class CountryState(
    populationByYear: Map[Int, Long] = Map.empty,
    populationGrowthByYear: Map[Int, Long] = Map.empty,
    averagePopulationGrowth: Long = 0
) extends State[CountryEvents, CountryState] {
  override def +(event: CountryEvents): CountryState =
    event match {
      case evt @ CountryEvents.AddedYearlyCountryPopulation(_, year, population) =>
        copy(
          populationByYear = populationByYear + ((year -> population))
        ).copy(
          populationGrowthByYear = CountryServices.populationGrowthByYear(evt, this)
        ).copy(
          averagePopulationGrowth = CountryServices.averagePopulationGrowth(this)
        )
    }
}
