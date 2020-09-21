package domain

object CountryServices {
  def topTenCountriesByGDP(countries: Seq[Country]): Seq[Country] =
    countries.sortBy(_.GDP.value).takeRight(11)
}