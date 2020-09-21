package application.commands

import domain.GDP
import domain_driven_design.cqrs.Command

trait CountryCommands

object CountryCommands {
  case class AddGDP(country: String, GDP: GDP) extends Command {
    override def entityId: String = country
  }
}
