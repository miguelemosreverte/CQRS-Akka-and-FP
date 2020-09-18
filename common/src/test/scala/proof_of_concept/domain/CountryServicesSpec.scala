package proof_of_concept.domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers.{be, convertToAnyMustWrapper}

class CountryServicesSpec extends AnyFlatSpec {
  "topTenCountriesByGDP given a list of countries" should "sort them by DGP and get the first 10" in {
    def countryFactory(range: Seq[Int]): Seq[Country] = range.map(i => Country(id = s"Country-$i", GDP = GDP(i)))
    val countries = countryFactory(0 to 100)
    CountryServices.topTenCountriesByGDP(countries) must be(countryFactory(90 to 100))
  }
}
