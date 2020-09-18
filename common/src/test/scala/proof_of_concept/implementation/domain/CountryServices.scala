package proof_of_concept.implementation.domain

object CountryServices {
  def topTenCountriesByGDP(countries: Seq[Country]): Seq[Country] =
    countries.sortBy(_.GDP.value).takeRight(11)
}
