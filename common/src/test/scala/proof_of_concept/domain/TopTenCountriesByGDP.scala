package proof_of_concept.domain

import domain_driven_design.Aggregate

case class TopTenCountriesByGDP(entities: Seq[Country]) extends Aggregate[Seq, String]
