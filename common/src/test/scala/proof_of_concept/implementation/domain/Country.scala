package proof_of_concept.implementation.domain

import domain_driven_design.building_blocks.Entity

case class Country(id: String, GDP: GDP) extends Entity[String]
