package proof_of_concept.domain

import domain_driven_design.Entity

case class Country(id: String, GDP: GDP) extends Entity[String]
