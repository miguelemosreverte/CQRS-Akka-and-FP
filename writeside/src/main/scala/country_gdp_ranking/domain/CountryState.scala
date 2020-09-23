package country_gdp_ranking.domain

import country_gdp_ranking.application.events.CountryEvents
import country_gdp_ranking.application.events.CountryEvents.AddedGDP
import domain_driven_design.cqrs.State

case class CountryState(GDP: GDP) extends State[CountryEvents, CountryState] {
  override def +(event: CountryEvents): CountryState =
    event match {
      case AddedGDP(_, gdp) =>
        copy(
          GDP = gdp
        )
    }
}
