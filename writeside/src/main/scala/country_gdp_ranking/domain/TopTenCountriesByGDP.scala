package country_gdp_ranking.domain

import domain_driven_design.building_blocks.Aggregate

case class TopTenCountriesByGDP(aggregateRoot: String, entities: Seq[Country]) extends Aggregate[Seq, String]
