package proof_of_concept.implementation.domain

import domain_driven_design.building_blocks.Aggregate

case class TopTenCountriesByGDP(entities: Seq[Country]) extends Aggregate[Seq, String]