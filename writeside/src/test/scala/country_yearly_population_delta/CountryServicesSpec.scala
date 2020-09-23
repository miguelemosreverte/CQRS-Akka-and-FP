package country_yearly_population_delta

import country_yearly_population_delta.application.commands.CountryCommands.AddYearlyCountryPopulation
import country_yearly_population_delta.application.events.CountryEvents.AddedYearlyCountryPopulation
import country_yearly_population_delta.domain.{Country, CountryServices, CountryState}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers.{be, convertToAnyMustWrapper}

class CountryServicesSpec extends AnyFlatSpec {

  val countryState: CountryState = Seq(
    AddedYearlyCountryPopulation("Argentina", 2000, 1000),
    AddedYearlyCountryPopulation("Argentina", 2001, 2000),
    AddedYearlyCountryPopulation("Argentina", 2002, 3000)
  ).foldLeft(CountryState())((state, event) => state + event)

  "CountryServices.populationGrowthByYear" should
  """ 
      give a map from year 
      to the population delta 
      between that year
      and the one before
    """ in {

    CountryServices.populationGrowthByYear(
      AddedYearlyCountryPopulation("Argentina", 2003, 4000),
      countryState
    )(2003) == 1000
  }

  "CountryServices.averagePopulationGrowth" should
  """ 
      give the average 
      of every year growth
      in population
    """ in {

    CountryServices.averagePopulationGrowth(
      countryState
    ) == 1000
  }
}
