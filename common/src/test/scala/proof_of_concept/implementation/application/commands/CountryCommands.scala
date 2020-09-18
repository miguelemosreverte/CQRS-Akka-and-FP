package proof_of_concept.implementation.application.commands

import domain_driven_design.cqrs.Command
import proof_of_concept.implementation.domain.GDP

trait CountryCommands

object CountryCommands {
  case class AddGDP(country: String, GDP: GDP) extends Command {
    override def entityId: String = country
  }
}
