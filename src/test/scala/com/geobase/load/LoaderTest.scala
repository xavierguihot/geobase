package com.geobase.load

import org.scalatest.FunSuite

/** Testing facility for the GeoBase data loader.
  *
  * @author Xavier Guihot
  * @since 2017-01
  */
class LoaderTest extends FunSuite {

  test("Load Airports and Cities") {

    val airportOrCityToDataMap = Loader.loadAirportsAndCities()

    assert(airportOrCityToDataMap("CDG").countryCode === "FR")
    assert(airportOrCityToDataMap("ORY").cityCode === "PAR")
  }

  test("Load Countries") {

    val countries = Loader.loadCountries()

    assert(countries("FR").currencyCode === "EUR")
    assert(countries("US").currencyCode === "USD")

    assert(countries("FR").continentCode === "EU")
    assert(countries("US").continentCode === "NA")

    assert(countries("FR").iataZone === "21")
    assert(countries("US").iataZone === "11")
  }

  test("Load Airlines") {

    val airlineToCountryMap = Loader.loadAirlines()

    assert(airlineToCountryMap("AF").countryCode === "FR")
    assert(airlineToCountryMap("AA").countryCode === "US")
  }
}
