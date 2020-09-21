package spec

import domain.{Country, CountryServices}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers.{be, convertToAnyMustWrapper}
import domain.GDP

class CountryServicesSpec extends AnyFlatSpec {
  "topTenCountriesByGDP given a list of countries" should "sort them by DGP and get the first 10" in {
    def countryFactory(range: Seq[Int]): Seq[Country] =
      range.map(i => domain.Country(id = s"Country-$i", GDP = domain.GDP(i)))
    val countries = countryFactory(0 to 100)
    CountryServices.topTenCountriesByGDP(countries) must be(countryFactory(90 to 100))
  }
}
