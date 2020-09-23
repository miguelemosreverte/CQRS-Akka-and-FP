package country_yearly_population_delta.domain

import domain_driven_design.building_blocks.Entity

case class Country(id: String, yearlyPopulationDelta: Long) extends Entity[String]
