package com.geobase.load

import scala.util.Success

import org.scalatest.FunSuite

/** Testing facility for the GeoBase data loader.
  *
  * @author Xavier Guihot
  * @since 2017-01
  */
class LoaderTest extends FunSuite {

  test("Airport and city loader") {

    val airportOrCityToDataMap = Loader.loadAirportsAndCities()

    assert(airportOrCityToDataMap("CDG").country === Success("FR"))
    assert(airportOrCityToDataMap("ORY").city === Success("PAR"))
  }

  test("Country loader") {

    val countries = Loader.loadCountries()

    assert(countries("FR").currency === Success("EUR"))
    assert(countries("US").currency === Success("USD"))

    assert(countries("FR").continent === Success("EU"))
    assert(countries("US").continent === Success("NA"))

    assert(countries("FR").iataZone === Success("21"))
    assert(countries("US").iataZone === Success("11"))
  }

  test("Airline loader") {

    val airlineToCountryMap = Loader.loadAirlines()

    assert(airlineToCountryMap("AF").country === Success("FR"))
    assert(airlineToCountryMap("AA").country === Success("US"))
  }
}
