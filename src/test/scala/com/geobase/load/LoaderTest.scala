package com.geobase.load

import scala.util.Success

import org.scalatest.FunSuite

/** Testing facility for the GeoBase data loader.
  *
  * @author Xavier Guihot
  * @since 2017-01
  */
class LoaderTest extends FunSuite {

  test("Airport and city loading") {

    val airportOrCityToDataMap = Loader.loadAirportsAndCities()

    assert(airportOrCityToDataMap("CDG").country === Success("FR"))
    assert(airportOrCityToDataMap("ORY").city === Success("PAR"))
  }

  test("Country loading") {

    val countries = Loader.loadCountries()

    assert(countries("FR").currency === Success("EUR"))
    assert(countries("US").currency === Success("USD"))

    assert(countries("FR").continent === Success("EU"))
    assert(countries("US").continent === Success("NA"))

    assert(countries("FR").iataZone === Success("21"))
    assert(countries("US").iataZone === Success("11"))
  }

  test("Airline loading") {

    val airlines = Loader.loadAirlines()

    assert(airlines("AF").country === Success("FR"))
    assert(airlines("AA").country === Success("US"))

    assert(airlines("AF").name === Success("Air France"))
    assert(airlines("AH").name === Success("Air Algerie"))
  }
}
