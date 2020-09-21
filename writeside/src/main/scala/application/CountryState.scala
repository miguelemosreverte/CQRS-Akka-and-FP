package application

import application.events.CountryEvents
import application.events.CountryEvents.AddedGDP
import domain.GDP
import domain_driven_design.cqrs.State

case class CountryState(GDP: GDP) extends State[CountryEvents, application.CountryState] {
  override def +(event: CountryEvents): application.CountryState =
    event match {
      case AddedGDP(_, gdp) =>
        copy(
          GDP = gdp
        )
    }
}
